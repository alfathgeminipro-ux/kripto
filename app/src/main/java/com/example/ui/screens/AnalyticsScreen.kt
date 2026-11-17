package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AnalyticsPeriod
import com.example.data.UiState
import com.example.data.UserWallet
import com.example.data.WalletTransaction
import com.example.ui.components.GlassCard
import com.example.ui.components.MonexAreaChart
import com.example.ui.components.MonexBarChart
import com.example.ui.components.SkeletonShimmerCard
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextMuted
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.formatRupiah
import com.example.util.formatSignedRupiah
import kotlin.math.abs

@Composable
fun AnalyticsScreen(
    walletState: UiState<UserWallet?>,
    transactionsState: UiState<List<WalletTransaction>>,
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit,
    onEditBudget: (Long) -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallet = (walletState as? UiState.Success)?.data
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()

    val totalBalance = wallet?.balanceIdr ?: 98_450_000L
    val incomeTotal = remember(transactions, selectedPeriod) {
        val txIn = transactions.filter { it.amountIdr > 0 }.sumOf { it.amountIdr }
        when (selectedPeriod) {
            AnalyticsPeriod.MONTHLY -> txIn.coerceAtLeast(14_500_000L)
            AnalyticsPeriod.WEEKLY -> (txIn / 2).coerceAtLeast(6_200_000L)
            AnalyticsPeriod.DAILY -> (txIn / 7).coerceAtLeast(1_450_000L)
        }
    }
    val expenseTotal = remember(transactions, selectedPeriod) {
        val txOut = transactions.filter { it.amountIdr < 0 }.sumOf { abs(it.amountIdr) }
        when (selectedPeriod) {
            AnalyticsPeriod.MONTHLY -> txOut.coerceAtLeast(7_485_000L)
            AnalyticsPeriod.WEEKLY -> (txOut / 2).coerceAtLeast(3_250_000L)
            AnalyticsPeriod.DAILY -> (txOut / 6).coerceAtLeast(780_000L)
        }
    }

    val areaPoints = remember(totalBalance, selectedPeriod) {
        when (selectedPeriod) {
            AnalyticsPeriod.MONTHLY -> listOf(
                totalBalance - 24_000_000L,
                totalBalance - 19_000_000L,
                totalBalance - 22_000_000L,
                totalBalance - 12_000_000L,
                totalBalance - 6_500_000L,
                totalBalance - 9_000_000L,
                totalBalance
            ).map { it.coerceAtLeast(5_000_000L) }
            AnalyticsPeriod.WEEKLY -> listOf(
                totalBalance - 8_200_000L,
                totalBalance - 6_500_000L,
                totalBalance - 7_100_000L,
                totalBalance - 3_400_000L,
                totalBalance - 1_200_000L,
                totalBalance
            ).map { it.coerceAtLeast(5_000_000L) }
            AnalyticsPeriod.DAILY -> listOf(
                totalBalance - 1_800_000L,
                totalBalance - 1_200_000L,
                totalBalance - 1_550_000L,
                totalBalance - 600_000L,
                totalBalance - 250_000L,
                totalBalance
            ).map { it.coerceAtLeast(5_000_000L) }
        }
    }
    val areaLabels = when (selectedPeriod) {
        AnalyticsPeriod.MONTHLY -> listOf("3", "8", "13", "18", "23", "28", "2")
        AnalyticsPeriod.WEEKLY -> listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Min")
        AnalyticsPeriod.DAILY -> listOf("08:00", "11:00", "14:00", "17:00", "20:00", "Kini")
    }

    val barData = when (selectedPeriod) {
        AnalyticsPeriod.MONTHLY -> listOf(0.45f, 0.72f, 0.38f, 0.88f, 0.64f, 0.52f)
        AnalyticsPeriod.WEEKLY -> listOf(0.55f, 0.42f, 0.90f, 0.62f, 0.76f, 0.48f)
        AnalyticsPeriod.DAILY -> listOf(0.30f, 0.65f, 0.92f, 0.50f, 0.70f, 0.40f)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analitik",
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

        // Sliding Glass Period Tabs
        item {
            PeriodSlidingTabs(
                selectedPeriod = selectedPeriod,
                onSelectPeriod = onSelectPeriod
            )
        }

        if (walletState is UiState.Loading) {
            item { SkeletonShimmerCard(height = 200.dp) }
            item { SkeletonShimmerCard(height = 170.dp) }
        } else {
            // Balance Area Chart Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total Saldo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonexTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formatRupiah(totalBalance),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MonexTextPrimary
                        )
                        Text(
                            text = "▲ ${formatRupiah(wallet?.incomeDeltaIdr ?: 14_500_000L)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MonexNeonLime
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    MonexAreaChart(
                        points = areaPoints,
                        labels = areaLabels,
                        accentColor = MonexNeonLime
                    )
                }
            }

            // Two-column Pemasukan & Arus Kas Positif cards
            item {
                val barFillProgress = remember { Animatable(0f) }
                LaunchedEffect(selectedPeriod, incomeTotal, expenseTotal) {
                    barFillProgress.snapTo(0f)
                    barFillProgress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        contentPadding = 16.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(incomeTotal),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MonexTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "▼ ${formatRupiah(expenseTotal)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexPink
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        MonexBarChart(
                            bars = barData,
                            highlightIndex = 3
                        )
                    }

                    GlassCard(
                        contentPadding = 16.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Arus Bersih",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val netFlow = (incomeTotal - expenseTotal).coerceAtLeast(0L)
                        Text(
                            text = formatRupiah(netFlow),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MonexTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(MonexNeonLime)
                            )
                            Text(
                                text = "Positif",
                                style = MaterialTheme.typography.labelSmall,
                                color = MonexTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        // Green income bar
                        val totalFlow = (incomeTotal + expenseTotal).coerceAtLeast(1L).toFloat()
                        val inRatio = (incomeTotal / totalFlow).coerceIn(0.25f, 0.95f) * barFillProgress.value
                        val outRatio = (expenseTotal / totalFlow).coerceIn(0.20f, 0.85f) * barFillProgress.value

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MonexSurfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(inRatio)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(MonexNeonLime)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MonexSurfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(outRatio)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(MonexPink)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatSignedRupiah(incomeTotal - expenseTotal),
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexNeonLime
                        )
                    }
                }
            }

            // Budget Card
            item {
                val budgetLimit = wallet?.monthlyBudgetIdr ?: 12_000_000L
                val remaining = (budgetLimit - expenseTotal).coerceAtLeast(0L)
                val overspent = expenseTotal > budgetLimit
                val progressRatio = (expenseTotal.toFloat() / budgetLimit.coerceAtLeast(1L).toFloat()).coerceIn(0.05f, 1f)

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Anggaran",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary
                        )
                        Text(
                            text = "Ubah batas",
                            style = MaterialTheme.typography.labelLarge,
                            color = MonexNeonLime,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_budget_button")
                                .clickable { onEditBudget(budgetLimit) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatRupiah(remaining),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MonexTextPrimary
                        )
                        Text(
                            text = "tersisa dari ${formatRupiah(budgetLimit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (overspent) MonexPink else MonexNeonLime)
                        )
                        Text(
                            text = if (overspent) "Melebihi anggaran" else "Anggaran terkendali",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (overspent) MonexPink else MonexNeonLime
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MonexSurfaceElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressRatio)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (overspent) MonexPink else MonexNeonLime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSlidingTabs(
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit
) {
    val periods = AnalyticsPeriod.entries
    val selectedIdx = periods.indexOf(selectedPeriod).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x16FFFFFF))
            .border(1.dp, MonexGlassBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / periods.size
        val pillOffset by animateDpAsState(
            targetValue = tabWidth * selectedIdx,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium),
            label = "analyticsTabPill"
        )

        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x28FFFFFF))
                .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(20.dp))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            periods.forEach { period ->
                val active = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("analytics_tab_${period.name.lowercase()}")
                        .clickable { onSelectPeriod(period) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) MonexTextPrimary else MonexTextMuted
                    )
                }
            }
        }
    }
}
