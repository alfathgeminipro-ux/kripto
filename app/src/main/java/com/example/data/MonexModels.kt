package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserWallet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val balanceIdr: Long = 0L,
    val incomeDeltaIdr: Long = 0L,
    val monthlyBudgetIdr: Long = 12_000_000L,
    val planName: String = "Standard",
    val cardLast4: String = "3377",
    val cardNumberMasked: String = "3456 5667 3211 3377",
    val cardExpiry: String = "01/29",
    val cardCvv: String = "277",
    val referralCode: String = "MONEX-3377",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val amountIdr: Long = 0L,
    val type: String = "transfer",
    val category: String = "Transfer",
    val iconKey: String = "transfer",
    val incomingTx: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TransferContact(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val handle: String = "",
    val avatarKey: String = "custom",
    val accentHex: String = "#C6FF1A",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CryptoHolding(
    @DocumentId val id: String = "",
    val userId: String = "",
    val symbol: String = "",
    val name: String = "",
    val amountUnits: Double = 0.0,
    val priceIdr: Long = 0L,
    val changePercent: Double = 0.0,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val totalValueIdr: Long
        get() = (amountUnits * priceIdr).toLong()
}

enum class AnalyticsPeriod(val label: String) {
    MONTHLY("Bulanan"),
    WEEKLY("Mingguan"),
    DAILY("Harian")
}

enum class MonexTab(val label: String) {
    HOME("Beranda"),
    ANALYTICS("Analitik"),
    CRYPTO("Kripto"),
    SETTINGS("Pengaturan")
}
