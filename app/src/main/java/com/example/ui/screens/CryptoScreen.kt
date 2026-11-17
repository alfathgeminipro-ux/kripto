package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CryptoHolding
import com.example.data.UiState
import com.example.ui.components.AnimatedCountUpRupiah
import com.example.ui.components.GlassCard
import com.example.ui.components.MonexSparkline
import com.example.ui.components.SkeletonShimmerCard
import com.example.ui.theme.MonexBlueOrb
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexPurpleOrb
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.formatCryptoUnits
import com.example.util.formatRupiah
import java.util.Locale

@Composable
fun CryptoScreen(
    cryptoState: UiState<List<CryptoHolding>>,
    onOpenCryptoTrade: (actionType: String, symbol: String) -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Kripto",
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

        when (cryptoState) {
            is UiState.Loading -> {
                item { SkeletonShimmerCard(height = 200.dp) }
                item { SkeletonShimmerCard(height = 160.dp) }
            }
            is UiState.Error -> {
                item {
                    GlassCard {
                        Text(
                            text = cryptoState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexPink
                        )
                    }
                }
            }
            is UiState.Success -> {
                val holdings = cryptoState.data
                val totalPortfolioIdr = holdings.sumOf { it.totalValueIdr }
                val btc = holdings.firstOrNull { it.symbol == "BTC" }
                val eth = holdings.firstOrNull { it.symbol == "ETH" }

                // Total Crypto Portfolio Card
                item {
                    GlassCard(
                        enableTilt = true,
                        accentGlow = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Total Portofolio Kripto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonexTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedCountUpRupiah(
                            targetAmountIdr = totalPortfolioIdr,
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+Rp 4.120.000 (+7,21%)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MonexNeonLime
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CryptoActionCircle(
                                icon = Icons.Rounded.TrendingUp,
                                label = "Trade",
                                tag = "crypto_trade_button",
                                onClick = { onOpenCryptoTrade("crypto_buy", btc?.symbol ?: "BTC") }
                            )
                            CryptoActionCircle(
                                icon = Icons.Rounded.ArrowUpward,
                                label = "Kirim",
                                tag = "crypto_send_button",
                                onClick = { onOpenCryptoTrade("crypto_send", btc?.symbol ?: "BTC") }
                            )
                            CryptoActionCircle(
                                icon = Icons.Rounded.ArrowDownward,
                                label = "Terima",
                                tag = "crypto_receive_button",
                                onClick = { onOpenCryptoTrade("crypto_receive", eth?.symbol ?: "ETH") }
                            )
                        }
                    }
                }

                // BTC & ETH Sparkline Cards
                if (btc != null && eth != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CryptoHighlightCard(
                                holding = btc,
                                sparkPoints = listOf(12f, 15f, 13.5f, 17f, 16.2f, 20f, 19.4f, 23f),
                                accent = MonexNeonLime,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCryptoTrade("crypto_buy", btc.symbol) }
                            )
                            CryptoHighlightCard(
                                holding = eth,
                                sparkPoints = listOf(22f, 20f, 21.5f, 17f, 18.2f, 14f, 15.1f, 11.8f),
                                accent = MonexPink,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCryptoTrade("crypto_buy", eth.symbol) }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Semua Kripto",
                        style = MaterialTheme.typography.titleMedium,
                        color = MonexTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(holdings, key = { it.symbol }) { holding ->
                    val isUp = holding.changePercent >= 0
                    val badgeColor = when (holding.symbol) {
                        "BTC" -> Color(0xFFF7931A)
                        "ETH" -> MonexBlueOrb
                        "SOL" -> MonexPurpleOrb
                        else -> MonexNeonLime
                    }
                    GlassCard(
                        contentPadding = 14.dp,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCryptoTrade("crypto_buy", holding.symbol) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (holding.symbol == "BTC") {
                                    Icon(
                                        imageVector = Icons.Rounded.CurrencyBitcoin,
                                        contentDescription = holding.name,
                                        tint = badgeColor
                                    )
                                } else {
                                    Text(
                                        text = holding.symbol.take(3),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = holding.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MonexTextPrimary
                                )
                                Text(
                                    text = formatCryptoUnits(holding.amountUnits, holding.symbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MonexTextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatRupiah(holding.totalValueIdr),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MonexTextPrimary
                                )
                                Text(
                                    text = "${if (isUp) "▲ +" else "▼ "}${String.format(Locale.US, "%.2f", holding.changePercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUp) MonexNeonLime else MonexPink
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CryptoActionCircle(
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MonexTextSecondary)
    }
}

@Composable
private fun CryptoHighlightCard(
    holding: CryptoHolding,
    sparkPoints: List<Float>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isUp = holding.changePercent >= 0
    GlassCard(
        contentPadding = 14.dp,
        modifier = modifier
    ) {
        Text(
            text = holding.symbol,
            style = MaterialTheme.typography.labelLarge,
            color = MonexTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatRupiah(holding.priceIdr),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MonexTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${if (isUp) "▲ +" else "▼ "}${String.format(Locale.US, "%.2f", holding.changePercent)}%",
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
        Spacer(modifier = Modifier.height(10.dp))
        MonexSparkline(points = sparkPoints, lineColor = accent)
    }
}
