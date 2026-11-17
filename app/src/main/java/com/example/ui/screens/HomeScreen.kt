package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.UiState
import com.example.data.UserWallet
import com.example.data.WalletTransaction
import com.example.ui.components.AnimatedCountUpRupiah
import com.example.ui.components.GlassCard
import com.example.ui.components.SkeletonShimmerCard
import com.example.ui.components.TransactionIconBadge
import com.example.ui.components.VirtualVisaCardSection
import com.example.ui.components.WalletActionSheetType
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.formatRupiah
import com.example.util.formatSignedRupiah
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    walletState: UiState<UserWallet?>,
    transactionsState: UiState<List<WalletTransaction>>,
    onOpenActionSheet: (WalletActionSheetType) -> Unit,
    onOpenNotifications: () -> Unit,
    onCopiedToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllTransfers by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Beranda",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MonexTextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, MonexGlassBorder, CircleShape)
                        .testTag("home_notifications_button")
                        .clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifikasi",
                        tint = MonexTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        when (walletState) {
            is UiState.Loading -> {
                item { SkeletonShimmerCard(height = 48.dp) }
                item { SkeletonShimmerCard(height = 220.dp) }
            }
            is UiState.Error -> {
                item {
                    GlassCard {
                        Text(
                            text = "Gagal memuat dompet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MonexPink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = walletState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary
                        )
                    }
                }
            }
            is UiState.Success -> {
                val wallet = walletState.data
                if (wallet == null) {
                    item { SkeletonShimmerCard(height = 220.dp) }
                } else {
                    // Pull-down Virtual VISA Card
                    item {
                        VirtualVisaCardSection(
                            wallet = wallet,
                            onCopied = onCopiedToast
                        )
                    }

                    // Total Saldo Card with 3D tilt & count-up
                    item {
                        val deltaFlash = remember { Animatable(0f) }
                        LaunchedEffect(wallet.incomeDeltaIdr) {
                            deltaFlash.snapTo(1f)
                            deltaFlash.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
                        }

                        GlassCard(
                            enableTilt = true,
                            accentGlow = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("total_balance_card")
                        ) {
                            Text(
                                text = "Total Saldo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MonexTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedCountUpRupiah(
                                targetAmountIdr = wallet.balanceIdr,
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MonexNeonLime.copy(alpha = 0.08f + deltaFlash.value * 0.28f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = formatSignedRupiah(wallet.incomeDeltaIdr),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MonexNeonLime
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                HomeActionButton(
                                    icon = Icons.Rounded.ArrowUpward,
                                    label = "Tarik",
                                    tag = "action_withdraw_button",
                                    onClick = { onOpenActionSheet(WalletActionSheetType.WITHDRAW) }
                                )
                                HomeActionButton(
                                    icon = Icons.Rounded.ArrowDownward,
                                    label = "Isi Saldo",
                                    tag = "action_topup_button",
                                    onClick = { onOpenActionSheet(WalletActionSheetType.TOP_UP) }
                                )
                                HomeActionButton(
                                    icon = Icons.Rounded.SwapHoriz,
                                    label = "Tukar",
                                    tag = "action_exchange_button",
                                    onClick = { onOpenActionSheet(WalletActionSheetType.EXCHANGE) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transfers Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transfer Terbaru",
                    style = MaterialTheme.typography.titleMedium,
                    color = MonexTextPrimary
                )
                Text(
                    text = if (showAllTransfers) "Tampilkan Ringkas" else "Lihat Semua",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MonexNeonLime,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { showAllTransfers = !showAllTransfers }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }

        when (transactionsState) {
            is UiState.Loading -> {
                items(3) { SkeletonShimmerCard(height = 72.dp) }
            }
            is UiState.Error -> {
                item {
                    GlassCard(contentPadding = 16.dp) {
                        Text(
                            text = transactionsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexPink
                        )
                    }
                }
            }
            is UiState.Success -> {
                val txList = if (showAllTransfers) {
                    transactionsState.data
                } else {
                    transactionsState.data.take(4)
                }
                if (txList.isEmpty()) {
                    item {
                        GlassCard(contentPadding = 18.dp) {
                            Text(
                                text = "Belum ada riwayat transfer. Mulai kirim uang atau isi saldo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MonexTextSecondary
                            )
                        }
                    }
                } else {
                    itemsIndexed(txList, key = { _, item -> item.id }) { index, tx ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(tx.id) {
                            delay((index.coerceAtMost(6)) * 70L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(240)) + slideInVertically(tween(260)) { it / 3 }
                        ) {
                            TransactionRowCard(tx = tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MonexSurfaceElevated)
                .border(1.dp, MonexGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MonexTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MonexTextSecondary
        )
    }
}

@Composable
fun TransactionRowCard(tx: WalletTransaction) {
    GlassCard(
        contentPadding = 14.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionIconBadge(iconKey = tx.iconKey)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MonexTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tx.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonexTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatSignedRupiah(tx.amountIdr),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (tx.amountIdr >= 0) MonexNeonLime else MonexTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MonexTextSecondary
                )
            }
        }
    }
}
