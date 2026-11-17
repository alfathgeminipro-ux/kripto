package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale

class MonexRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
        if (expectedUserId != null && uid != expectedUserId) {
            throw IllegalStateException("Sesi akun berubah; operasi dibatalkan.")
        }
        return uid
    }

    fun observeWallet(userId: String): Flow<UserWallet?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(UserWallet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<WalletTransaction>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeContacts(userId: String): Flow<List<TransferContact>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/contacts"
        emitAll(
            db.collection("users").document(uid).collection("contacts")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TransferContact::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCryptoHoldings(userId: String): Flow<List<CryptoHolding>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/cryptoHoldings"
        emitAll(
            db.collection("users").document(uid).collection("cryptoHoldings")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CryptoHolding::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.totalValueIdr }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureSeededForUser(
        userId: String,
        displayName: String,
        email: String,
        photoUrl: String
    ): Result<Unit> {
        val userDocPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val existing = userRef.get().await()
            if (existing.exists()) {
                return Result.success(Unit)
            }

            val safeName = displayName.trim().ifBlank { "Pengguna Monex" }.take(80)
            val rawHandle = email.substringBefore("@").ifBlank { safeName.lowercase(Locale.ROOT) }
                .replace(Regex("[^a-zA-Z0-9._]"), "")
                .take(30)
                .ifBlank { "pengguna" }
            val username = "@$rawHandle".take(40)
            val referralCode = "MONEX-${uid.take(4).uppercase(Locale.ROOT)}"

            val batch = db.batch()
            batch.set(
                userRef,
                mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "username" to username,
                    "photoUrl" to photoUrl.take(500),
                    "balanceIdr" to 98_450_000L,
                    "incomeDeltaIdr" to 14_500_000L,
                    "monthlyBudgetIdr" to 12_000_000L,
                    "planName" to "Standard",
                    "cardLast4" to "3377",
                    "cardNumberMasked" to "3456 5667 3211 3377",
                    "cardExpiry" to "01/29",
                    "cardCvv" to "277",
                    "referralCode" to referralCode,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            val initialTransactions = listOf(
                Triple("tx_bolt", "Bolt Food", -425_000L) to listOf("Hari ini, 12:15", "transfer", "Makanan", "bolt", false),
                Triple("tx_marie", "Marie", -1_550_000L) to listOf("26 Mar, 16:24", "transfer", "Transfer", "marie", false),
                Triple("tx_uber", "Uber", -310_000L) to listOf("Kemarin, 16:42", "transfer", "Transportasi", "uber", false),
                Triple("tx_gaji", "Gaji Bulanan", 14_500_000L) to listOf("24 Mar, 09:00", "topup", "Pemasukan", "topup", true),
                Triple("tx_btc", "Pembelian BTC", -5_200_000L) to listOf("22 Mar, 19:10", "crypto_buy", "Kripto", "btc", false)
            )
            initialTransactions.forEach { (idTitleAmount, meta) ->
                val (txId, title, amount) = idTitleAmount
                val subtitle = meta[0] as String
                val type = meta[1] as String
                val category = meta[2] as String
                val iconKey = meta[3] as String
                val incoming = meta[4] as Boolean
                batch.set(
                    userRef.collection("transactions").document(txId),
                    mapOf(
                        "userId" to uid,
                        "title" to title,
                        "subtitle" to subtitle,
                        "amountIdr" to amount,
                        "type" to type,
                        "category" to category,
                        "iconKey" to iconKey,
                        "incomingTx" to incoming,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val initialContacts = listOf(
                listOf("c_james", "James", "@james.m", "james", "#C6FF1A"),
                listOf("c_marie", "Marie", "@marie.k", "marie", "#FF4D88"),
                listOf("c_john", "John", "@john.d", "john", "#2E7CF6")
            )
            initialContacts.forEach { c ->
                batch.set(
                    userRef.collection("contacts").document(c[0]),
                    mapOf(
                        "userId" to uid,
                        "name" to c[1],
                        "handle" to c[2],
                        "avatarKey" to c[3],
                        "accentHex" to c[4],
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val initialCrypto = listOf(
                listOf("BTC", "Bitcoin", 0.0325, 1_325_000_000L, 2.20),
                listOf("ETH", "Ethereum", 0.85, 29_150_000L, -3.99),
                listOf("SOL", "Solana", 6.40, 2_450_000L, 5.64),
                listOf("ADA", "Cardano", 1250.0, 11_200L, 1.42)
            )
            initialCrypto.forEach { h ->
                val symbol = h[0] as String
                batch.set(
                    userRef.collection("cryptoHoldings").document(symbol),
                    mapOf(
                        "userId" to uid,
                        "symbol" to symbol,
                        "name" to (h[1] as String),
                        "amountUnits" to (h[2] as Double),
                        "priceIdr" to (h[3] as Long),
                        "changePercent" to (h[4] as Double),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocPath)
            Result.failure(e)
        }
    }

    suspend fun sendMoney(
        userId: String,
        recipientName: String,
        avatarKey: String,
        amountIdr: Long,
        note: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(amountIdr > 0L) { "Nominal harus lebih dari Rp 0." }
            require(recipientName.isNotBlank()) { "Pilih penerima terlebih dahulu." }
            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()

            db.runTransaction { tx ->
                val walletSnap = tx.get(userRef)
                val currentBalance = walletSnap.getLong("balanceIdr") ?: 0L
                if (currentBalance < amountIdr) {
                    throw IllegalStateException("Saldo tidak mencukupi untuk transfer ini.")
                }
                val newBalance = currentBalance - amountIdr
                tx.update(
                    userRef,
                    mapOf(
                        "balanceIdr" to newBalance,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "title" to recipientName.trim().take(80),
                        "subtitle" to note.ifBlank { "Transfer instan Monex" }.take(80),
                        "amountIdr" to -amountIdr,
                        "type" to "transfer",
                        "category" to "Transfer",
                        "iconKey" to avatarKey.ifBlank { "transfer" }.take(30),
                        "incomingTx" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun topUp(userId: String, amountIdr: Long, methodLabel: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(amountIdr > 0L) { "Nominal isi saldo harus lebih dari Rp 0." }
            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()

            db.runTransaction { tx ->
                val walletSnap = tx.get(userRef)
                val currentBalance = walletSnap.getLong("balanceIdr") ?: 0L
                val currentIncome = walletSnap.getLong("incomeDeltaIdr") ?: 0L
                tx.update(
                    userRef,
                    mapOf(
                        "balanceIdr" to (currentBalance + amountIdr),
                        "incomeDeltaIdr" to (currentIncome + amountIdr),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "title" to "Isi Saldo",
                        "subtitle" to methodLabel.ifBlank { "Transfer Bank Virtual" }.take(80),
                        "amountIdr" to amountIdr,
                        "type" to "topup",
                        "category" to "Pemasukan",
                        "iconKey" to "topup",
                        "incomingTx" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun withdraw(userId: String, amountIdr: Long, bankLabel: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(amountIdr > 0L) { "Nominal penarikan harus lebih dari Rp 0." }
            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()

            db.runTransaction { tx ->
                val walletSnap = tx.get(userRef)
                val currentBalance = walletSnap.getLong("balanceIdr") ?: 0L
                if (currentBalance < amountIdr) {
                    throw IllegalStateException("Saldo tidak mencukupi untuk penarikan.")
                }
                tx.update(
                    userRef,
                    mapOf(
                        "balanceIdr" to (currentBalance - amountIdr),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "title" to "Tarik Dana",
                        "subtitle" to bankLabel.ifBlank { "Ke Rekening Bank" }.take(80),
                        "amountIdr" to -amountIdr,
                        "type" to "withdraw",
                        "category" to "Penarikan",
                        "iconKey" to "withdraw",
                        "incomingTx" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun exchangeFunds(
        userId: String,
        amountIdr: Long,
        targetCurrency: String,
        convertedPreview: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(amountIdr > 0L) { "Nominal tukar harus lebih dari Rp 0." }
            val userRef = db.collection("users").document(uid)
            val txRef = userRef.collection("transactions").document()

            db.runTransaction { tx ->
                val walletSnap = tx.get(userRef)
                val currentBalance = walletSnap.getLong("balanceIdr") ?: 0L
                if (currentBalance < amountIdr) {
                    throw IllegalStateException("Saldo tidak mencukupi untuk penukaran.")
                }
                tx.update(
                    userRef,
                    mapOf(
                        "balanceIdr" to (currentBalance - amountIdr),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "title" to "Tukar ke $targetCurrency",
                        "subtitle" to "Konversi • $convertedPreview".take(80),
                        "amountIdr" to -amountIdr,
                        "type" to "exchange",
                        "category" to "Tukar Valas",
                        "iconKey" to "exchange",
                        "incomingTx" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun tradeCrypto(
        userId: String,
        symbol: String,
        actionType: String, // "crypto_buy", "crypto_sell", "crypto_send", "crypto_receive"
        amountIdr: Long
    ): Result<Unit> {
        val path = "users/$userId/cryptoHoldings/$symbol"
        return try {
            val uid = requireUserId(userId)
            require(amountIdr > 0L) { "Nominal harus lebih dari Rp 0." }
            val userRef = db.collection("users").document(uid)
            val holdingRef = userRef.collection("cryptoHoldings").document(symbol)
            val txRef = userRef.collection("transactions").document()

            db.runTransaction { tx ->
                val walletSnap = tx.get(userRef)
                val holdingSnap = tx.get(holdingRef)
                val currentBalance = walletSnap.getLong("balanceIdr") ?: 0L
                val priceIdr = holdingSnap.getLong("priceIdr") ?: 1_000_000L
                val currentUnits = holdingSnap.getDouble("amountUnits") ?: 0.0
                val deltaUnits = amountIdr.toDouble() / priceIdr.toDouble()

                when (actionType) {
                    "crypto_buy" -> {
                        if (currentBalance < amountIdr) {
                            throw IllegalStateException("Saldo tidak mencukupi untuk membeli $symbol.")
                        }
                        tx.update(
                            userRef,
                            mapOf(
                                "balanceIdr" to (currentBalance - amountIdr),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        tx.update(
                            holdingRef,
                            mapOf(
                                "amountUnits" to (currentUnits + deltaUnits),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    "crypto_sell" -> {
                        if (currentUnits < deltaUnits) {
                            throw IllegalStateException("Kepemilikan $symbol tidak mencukupi untuk dijual.")
                        }
                        tx.update(
                            userRef,
                            mapOf(
                                "balanceIdr" to (currentBalance + amountIdr),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        tx.update(
                            holdingRef,
                            mapOf(
                                "amountUnits" to (currentUnits - deltaUnits).coerceAtLeast(0.0),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    "crypto_send" -> {
                        if (currentUnits < deltaUnits) {
                            throw IllegalStateException("Kepemilikan $symbol tidak mencukupi untuk dikirim.")
                        }
                        tx.update(
                            holdingRef,
                            mapOf(
                                "amountUnits" to (currentUnits - deltaUnits).coerceAtLeast(0.0),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    "crypto_receive" -> {
                        tx.update(
                            holdingRef,
                            mapOf(
                                "amountUnits" to (currentUnits + deltaUnits),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    else -> throw IllegalArgumentException("Tipe aksi kripto tidak dikenal.")
                }

                val title = when (actionType) {
                    "crypto_buy" -> "Beli $symbol"
                    "crypto_sell" -> "Jual $symbol"
                    "crypto_send" -> "Kirim $symbol"
                    else -> "Terima $symbol"
                }
                val signedAmount = when (actionType) {
                    "crypto_sell", "crypto_receive" -> amountIdr
                    else -> -amountIdr
                }
                val incoming = actionType == "crypto_sell" || actionType == "crypto_receive"

                tx.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "title" to title,
                        "subtitle" to "${String.format(Locale.US, "%.5f", deltaUnits)} $symbol",
                        "amountIdr" to signedAmount,
                        "type" to actionType,
                        "category" to "Kripto",
                        "iconKey" to symbol.lowercase(Locale.ROOT),
                        "incomingTx" to incoming,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun addContact(userId: String, name: String, handle: String): Result<Unit> {
        val path = "users/$userId/contacts"
        return try {
            val uid = requireUserId(userId)
            val trimmedName = name.trim().take(60)
            require(trimmedName.isNotEmpty()) { "Nama kontak tidak boleh kosong." }
            val safeHandle = handle.trim().let { if (it.startsWith("@")) it else "@$it" }
                .ifBlank { "@${trimmedName.lowercase(Locale.ROOT).replace(" ", "")}" }
                .take(40)
            val accents = listOf("#C6FF1A", "#FF4D88", "#2E7CF6", "#7B61FF")
            val accent = accents[trimmedName.length % accents.size]
            val docRef = db.collection("users").document(uid).collection("contacts").document()
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "name" to trimmedName,
                    "handle" to safeHandle,
                    "avatarKey" to "custom",
                    "accentHex" to accent,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateMonthlyBudget(userId: String, newBudgetIdr: Long): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(newBudgetIdr >= 100_000L) { "Anggaran minimal Rp 100.000." }
            db.collection("users").document(uid).update(
                mapOf(
                    "monthlyBudgetIdr" to newBudgetIdr,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
