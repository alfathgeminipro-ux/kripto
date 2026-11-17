package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.MonexRepository
import com.example.data.MonexTab
import com.example.data.UiState
import com.example.ui.auth.signOutMonex
import com.example.ui.components.AddContactBottomSheet
import com.example.ui.components.AnimatedOrbBackground
import com.example.ui.components.BudgetEditBottomSheet
import com.example.ui.components.CryptoTradeBottomSheet
import com.example.ui.components.FloatingGlassNavBar
import com.example.ui.components.InfoDetailBottomSheet
import com.example.ui.components.MonexNavigationRail
import com.example.ui.components.WalletActionBottomSheet
import com.example.ui.components.WalletActionSheetType
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CryptoScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SendMoneyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MonexBg
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurface
import com.example.ui.theme.MonexTextPrimary
import com.example.util.formatRupiah
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun MonexAppShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    userPhotoUrl: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MonexViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MonexViewModel(MonexRepository(db), currentUserId)
            }
        }
    )

    LaunchedEffect(currentUserId) {
        viewModel.ensureSeeded(userDisplayName, userEmail, userPhotoUrl)
    }

    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val contactsState by viewModel.contactsState.collectAsStateWithLifecycle()
    val cryptoState by viewModel.cryptoState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showSendMoney by viewModel.showSendMoney.collectAsStateWithLifecycle()
    val analyticsPeriod by viewModel.analyticsPeriod.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val banner by viewModel.banner.collectAsStateWithLifecycle()
    val sendSuccess by viewModel.sendSuccess.collectAsStateWithLifecycle()

    // Modal sheet states
    var walletSheetType by remember { mutableStateOf<WalletActionSheetType?>(null) }
    var cryptoTradeRequest by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddContactSheet by remember { mutableStateOf(false) }
    var editBudgetInitial by remember { mutableStateOf<Long?>(null) }
    var infoSheet by remember { mutableStateOf<Pair<String, List<Pair<String, String>>>?>(null) }

    // Welcome toast right after sign-in
    var showWelcomePill by remember(currentUserId) { mutableStateOf(true) }
    LaunchedEffect(currentUserId) {
        delay(2800)
        showWelcomePill = false
    }

    // Auto-dismiss banner
    LaunchedEffect(banner) {
        if (banner != null) {
            delay(2800)
            viewModel.clearBanner()
        }
    }

    val currentBalanceIdr = (walletState as? UiState.Success)?.data?.balanceIdr ?: 0L
    val openNotificationsSheet = {
        val recent = (transactionsState as? UiState.Success)?.data.orEmpty().take(3)
        val bullets = if (recent.isEmpty()) {
            listOf("Dompet Aktif" to "Akun Monex Anda siap untuk transfer dan investasi kripto.")
        } else {
            recent.map { it.title to "${it.subtitle} • ${formatRupiah(it.amountIdr)}" }
        }
        infoSheet = "Aktivitas Terbaru" to bullets
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonexBg)
    ) {
        AnimatedOrbBackground()

        if (showSendMoney) {
            SendMoneyScreen(
                walletState = walletState,
                contactsState = contactsState,
                isProcessing = isProcessing,
                sendSuccess = sendSuccess,
                onBack = { viewModel.closeSendMoney() },
                onAddNewContact = { showAddContactSheet = true },
                onSendMoney = { name, avatar, amt, note ->
                    viewModel.sendMoney(name, avatar, amt, note)
                },
                onDoneAfterSuccess = {
                    viewModel.closeSendMoney()
                    viewModel.selectTab(MonexTab.HOME)
                }
            )
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp

                if (isWideScreen) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        MonexNavigationRail(
                            selectedTab = selectedTab,
                            onSelectTab = viewModel::selectTab,
                            onOpenSendMoney = viewModel::openSendMoney
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .statusBarsPadding(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            TabContentArea(
                                selectedTab = selectedTab,
                                walletState = walletState,
                                transactionsState = transactionsState,
                                cryptoState = cryptoState,
                                analyticsPeriod = analyticsPeriod,
                                userDisplayName = userDisplayName,
                                userPhotoUrl = userPhotoUrl,
                                onSelectPeriod = viewModel::selectAnalyticsPeriod,
                                onOpenWalletSheet = { walletSheetType = it },
                                onOpenCryptoTrade = { action, sym -> cryptoTradeRequest = action to sym },
                                onEditBudget = { editBudgetInitial = it },
                                onOpenNotifications = openNotificationsSheet,
                                onOpenInfoSheet = { title, bullets -> infoSheet = title to bullets },
                                onToast = viewModel::showInfo,
                                onSignOut = { signOutMonex(credentialManager, scope) },
                                modifier = Modifier.widthIn(max = 620.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        TabContentArea(
                            selectedTab = selectedTab,
                            walletState = walletState,
                            transactionsState = transactionsState,
                            cryptoState = cryptoState,
                            analyticsPeriod = analyticsPeriod,
                            userDisplayName = userDisplayName,
                            userPhotoUrl = userPhotoUrl,
                            onSelectPeriod = viewModel::selectAnalyticsPeriod,
                            onOpenWalletSheet = { walletSheetType = it },
                            onOpenCryptoTrade = { action, sym -> cryptoTradeRequest = action to sym },
                            onEditBudget = { editBudgetInitial = it },
                            onOpenNotifications = openNotificationsSheet,
                            onOpenInfoSheet = { title, bullets -> infoSheet = title to bullets },
                            onToast = viewModel::showInfo,
                            onSignOut = { signOutMonex(credentialManager, scope) },
                            modifier = Modifier.fillMaxSize()
                        )

                        FloatingGlassNavBar(
                            selectedTab = selectedTab,
                            onSelectTab = viewModel::selectTab,
                            onOpenSendMoney = viewModel::openSendMoney,
                            onQuickTopUp = { walletSheetType = WalletActionSheetType.TOP_UP },
                            onQuickExchange = { walletSheetType = WalletActionSheetType.EXCHANGE },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

        // Welcome Floating Pill
        AnimatedVisibility(
            visible = showWelcomePill,
            enter = fadeIn(tween(300)) + slideInVertically { -it },
            exit = fadeOut(tween(300)) + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            val firstName = userDisplayName.trim().substringBefore(" ").ifBlank { "Sobat Monex" }
            Surface(
                color = MonexSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MonexNeonLime.copy(alpha = 0.55f))
            ) {
                Text(
                    text = "Selamat datang, $firstName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MonexNeonLime,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        // Action Toast / Error Banner
        AnimatedVisibility(
            visible = banner != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            banner?.let { b ->
                val accent = if (b.isError) MonexPink else MonexNeonLime
                Surface(
                    color = MonexSurface.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = b.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextPrimary
                        )
                    }
                }
            }
        }
    }

    // Modal Sheets
    walletSheetType?.let { type ->
        WalletActionBottomSheet(
            type = type,
            currentBalanceIdr = currentBalanceIdr,
            onDismiss = { walletSheetType = null },
            onConfirmTopUp = { amt, method -> viewModel.topUp(amt, method) },
            onConfirmWithdraw = { amt, bank -> viewModel.withdraw(amt, bank) },
            onConfirmExchange = { amt, curr, prev -> viewModel.exchangeFunds(amt, curr, prev) }
        )
    }

    cryptoTradeRequest?.let { (actionType, symbol) ->
        val holdings = (cryptoState as? UiState.Success)?.data.orEmpty()
        CryptoTradeBottomSheet(
            holdings = holdings,
            initialActionType = actionType,
            initialSymbol = symbol,
            currentBalanceIdr = currentBalanceIdr,
            onDismiss = { cryptoTradeRequest = null },
            onConfirmTrade = { sym, act, amt -> viewModel.tradeCrypto(sym, act, amt) }
        )
    }

    if (showAddContactSheet) {
        AddContactBottomSheet(
            onDismiss = { showAddContactSheet = false },
            onConfirmAdd = { name, handle -> viewModel.addContact(name, handle) }
        )
    }

    editBudgetInitial?.let { current ->
        BudgetEditBottomSheet(
            currentBudgetIdr = current,
            onDismiss = { editBudgetInitial = null },
            onConfirmBudget = { updated -> viewModel.updateMonthlyBudget(updated) }
        )
    }

    infoSheet?.let { (title, bullets) ->
        InfoDetailBottomSheet(
            title = title,
            bullets = bullets,
            onDismiss = { infoSheet = null }
        )
    }
}

@Composable
private fun TabContentArea(
    selectedTab: MonexTab,
    walletState: UiState<com.example.data.UserWallet?>,
    transactionsState: UiState<List<com.example.data.WalletTransaction>>,
    cryptoState: UiState<List<com.example.data.CryptoHolding>>,
    analyticsPeriod: com.example.data.AnalyticsPeriod,
    userDisplayName: String,
    userPhotoUrl: String,
    onSelectPeriod: (com.example.data.AnalyticsPeriod) -> Unit,
    onOpenWalletSheet: (WalletActionSheetType) -> Unit,
    onOpenCryptoTrade: (String, String) -> Unit,
    onEditBudget: (Long) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenInfoSheet: (String, List<Pair<String, String>>) -> Unit,
    onToast: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally(tween(240)) { it / 12 }) togetherWith
                (fadeOut(tween(160)) + slideOutHorizontally(tween(180)) { -it / 12 })
        },
        modifier = modifier,
        label = "tabTransition"
    ) { tab ->
        when (tab) {
            MonexTab.HOME -> HomeScreen(
                walletState = walletState,
                transactionsState = transactionsState,
                onOpenActionSheet = onOpenWalletSheet,
                onOpenNotifications = onOpenNotifications,
                onCopiedToast = onToast
            )
            MonexTab.ANALYTICS -> AnalyticsScreen(
                walletState = walletState,
                transactionsState = transactionsState,
                selectedPeriod = analyticsPeriod,
                onSelectPeriod = onSelectPeriod,
                onEditBudget = onEditBudget,
                onOpenNotifications = onOpenNotifications
            )
            MonexTab.CRYPTO -> CryptoScreen(
                cryptoState = cryptoState,
                onOpenCryptoTrade = onOpenCryptoTrade,
                onOpenNotifications = onOpenNotifications
            )
            MonexTab.SETTINGS -> SettingsScreen(
                walletState = walletState,
                fallbackName = userDisplayName,
                fallbackPhotoUrl = userPhotoUrl,
                onOpenInfoSheet = onOpenInfoSheet,
                onCopiedMessage = onToast,
                onSignOut = onSignOut
            )
        }
    }
}
