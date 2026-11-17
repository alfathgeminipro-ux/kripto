package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnalyticsPeriod
import com.example.data.CryptoHolding
import com.example.data.MonexRepository
import com.example.data.MonexTab
import com.example.data.TransferContact
import com.example.data.UiState
import com.example.data.UserWallet
import com.example.data.WalletTransaction
import com.example.data.friendlyIndonesianError
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActionBanner(
    val message: String,
    val isError: Boolean = false
)

data class SendSuccessEvent(
    val recipientName: String,
    val avatarKey: String,
    val amountIdr: Long
)

class MonexViewModel(
    private val repository: MonexRepository,
    private val currentUserId: String
) : ViewModel() {

    val walletState: StateFlow<UiState<UserWallet?>> = repository.observeWallet(currentUserId)
        .map<UserWallet?, UiState<UserWallet?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(UiState.Error(friendlyIndonesianError(firestoreError ?: error)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val transactionsState: StateFlow<UiState<List<WalletTransaction>>> = repository.observeTransactions(currentUserId)
        .map<List<WalletTransaction>, UiState<List<WalletTransaction>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(UiState.Error(friendlyIndonesianError(firestoreError ?: error)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val contactsState: StateFlow<UiState<List<TransferContact>>> = repository.observeContacts(currentUserId)
        .map<List<TransferContact>, UiState<List<TransferContact>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(UiState.Error(friendlyIndonesianError(firestoreError ?: error)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val cryptoState: StateFlow<UiState<List<CryptoHolding>>> = repository.observeCryptoHoldings(currentUserId)
        .map<List<CryptoHolding>, UiState<List<CryptoHolding>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(UiState.Error(friendlyIndonesianError(firestoreError ?: error)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _selectedTab = MutableStateFlow(MonexTab.HOME)
    val selectedTab: StateFlow<MonexTab> = _selectedTab.asStateFlow()

    private val _showSendMoney = MutableStateFlow(false)
    val showSendMoney: StateFlow<Boolean> = _showSendMoney.asStateFlow()

    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.MONTHLY)
    val analyticsPeriod: StateFlow<AnalyticsPeriod> = _analyticsPeriod.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _banner = MutableStateFlow<ActionBanner?>(null)
    val banner: StateFlow<ActionBanner?> = _banner.asStateFlow()

    private val _sendSuccess = MutableStateFlow<SendSuccessEvent?>(null)
    val sendSuccess: StateFlow<SendSuccessEvent?> = _sendSuccess.asStateFlow()

    fun ensureSeeded(displayName: String, email: String, photoUrl: String) {
        viewModelScope.launch {
            val result = repository.ensureSeededForUser(currentUserId, displayName, email, photoUrl)
            if (result.isFailure) {
                _banner.value = ActionBanner(
                    message = friendlyIndonesianError(result.exceptionOrNull()),
                    isError = true
                )
            }
        }
    }

    fun selectTab(tab: MonexTab) {
        _showSendMoney.value = false
        _selectedTab.value = tab
    }

    fun openSendMoney() {
        _showSendMoney.value = true
    }

    fun closeSendMoney() {
        _showSendMoney.value = false
        _sendSuccess.value = null
    }

    fun selectAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
    }

    fun clearBanner() {
        _banner.value = null
    }

    fun clearSendSuccess() {
        _sendSuccess.value = null
    }

    fun showInfo(message: String) {
        _banner.value = ActionBanner(message = message, isError = false)
    }

    fun sendMoney(recipientName: String, avatarKey: String, amountIdr: Long, note: String) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.sendMoney(currentUserId, recipientName, avatarKey, amountIdr, note)
            _isProcessing.value = false
            if (result.isSuccess) {
                _sendSuccess.value = SendSuccessEvent(recipientName, avatarKey, amountIdr)
            } else {
                _banner.value = ActionBanner(
                    message = friendlyIndonesianError(result.exceptionOrNull()),
                    isError = true
                )
            }
        }
    }

    fun topUp(amountIdr: Long, methodLabel: String) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.topUp(currentUserId, amountIdr, methodLabel)
            _isProcessing.value = false
            _banner.value = if (result.isSuccess) {
                ActionBanner("Isi saldo berhasil ditambahkan ke dompet Monex.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    fun withdraw(amountIdr: Long, bankLabel: String) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.withdraw(currentUserId, amountIdr, bankLabel)
            _isProcessing.value = false
            _banner.value = if (result.isSuccess) {
                ActionBanner("Penarikan dana ke $bankLabel sedang diproses.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    fun exchangeFunds(amountIdr: Long, targetCurrency: String, convertedPreview: String) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.exchangeFunds(currentUserId, amountIdr, targetCurrency, convertedPreview)
            _isProcessing.value = false
            _banner.value = if (result.isSuccess) {
                ActionBanner("Penukaran ke $targetCurrency ($convertedPreview) berhasil.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    fun tradeCrypto(symbol: String, actionType: String, amountIdr: Long) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = repository.tradeCrypto(currentUserId, symbol, actionType, amountIdr)
            _isProcessing.value = false
            val actionLabel = when (actionType) {
                "crypto_buy" -> "Pembelian $symbol"
                "crypto_sell" -> "Penjualan $symbol"
                "crypto_send" -> "Pengiriman $symbol"
                else -> "Penerimaan $symbol"
            }
            _banner.value = if (result.isSuccess) {
                ActionBanner("$actionLabel berhasil diperbarui di portofolio.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    fun addContact(name: String, handle: String) {
        viewModelScope.launch {
            val result = repository.addContact(currentUserId, name, handle)
            _banner.value = if (result.isSuccess) {
                ActionBanner("Kontak $name ditambahkan.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    fun updateMonthlyBudget(newBudgetIdr: Long) {
        viewModelScope.launch {
            val result = repository.updateMonthlyBudget(currentUserId, newBudgetIdr)
            _banner.value = if (result.isSuccess) {
                ActionBanner("Batas anggaran bulanan diperbarui.")
            } else {
                ActionBanner(friendlyIndonesianError(result.exceptionOrNull()), isError = true)
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
}
