package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UiState
import com.example.data.UserWallet
import com.example.ui.components.AvatarView
import com.example.ui.components.GlassCard
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextMuted
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary

@Composable
fun SettingsScreen(
    walletState: UiState<UserWallet?>,
    fallbackName: String,
    fallbackPhotoUrl: String,
    onOpenInfoSheet: (title: String, bullets: List<Pair<String, String>>) -> Unit,
    onCopiedMessage: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallet = (walletState as? UiState.Success)?.data
    val displayName = wallet?.displayName?.ifBlank { fallbackName } ?: fallbackName.ifBlank { "Pengguna Monex" }
    val username = wallet?.username?.ifBlank { "@pengguna" } ?: "@pengguna"
    val photoUrl = wallet?.photoUrl?.ifBlank { fallbackPhotoUrl } ?: fallbackPhotoUrl
    val planName = wallet?.planName ?: "Standard"
    val referralCode = wallet?.referralCode ?: "MONEX-3377"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                color = MonexTextPrimary
            )
        }

        // User Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarView(
                    avatarKey = "user",
                    photoUrl = photoUrl,
                    fallbackName = displayName,
                    size = 76.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MonexTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonexTextSecondary
                )
            }
        }

        // Plan & Referral Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    contentPadding = 16.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onOpenInfoSheet(
                                "Paket $planName",
                                listOf(
                                    "Transfer Instan Gratis" to "Kirim Rupiah antar pengguna Monex tanpa biaya admin.",
                                    "Kartu VISA Virtual" to "Kartu virtual dummy siap digunakan untuk simulasi pembayaran online.",
                                    "Analitik Real-Time" to "Pantau pemasukan, pengeluaran, dan batas anggaran bulanan Anda."
                                )
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MonexNeonLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MonexOnNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonexTextPrimary
                    )
                    Text(
                        text = "Paket Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonexTextSecondary
                    )
                }

                GlassCard(
                    contentPadding = 16.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("referral_card_button")
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Kode Referral Monex", referralCode))
                            onCopiedMessage("Kode referral $referralCode disalin!")
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MonexSurfaceElevated)
                            .border(1.dp, MonexGlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = MonexNeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Referral",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonexTextPrimary
                    )
                    Text(
                        text = "Ajak & dapat hadiah",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonexTextSecondary
                    )
                }
            }
        }

        // Settings Menu Glass Card
        item {
            GlassCard(contentPadding = 6.dp, modifier = Modifier.fillMaxWidth()) {
                SettingsMenuItem(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    title = "Bantuan",
                    onClick = {
                        onOpenInfoSheet(
                            "Pusat Bantuan Monex",
                            listOf(
                                "Keamanan Kartu Virtual" to "Seluruh nomor kartu di Monex adalah data simulasi dummy untuk keamanan demo.",
                                "Sinkronisasi Akun Google" to "Saldo dan riwayat Anda disimpan aman di Cloud Firestore sesuai akun Google."
                            )
                        )
                    }
                )
                HorizontalDivider(color = MonexGlassBorder, modifier = Modifier.padding(horizontal = 14.dp))
                SettingsMenuItem(
                    icon = Icons.Rounded.PersonOutline,
                    title = "Akun",
                    onClick = {
                        onOpenInfoSheet(
                            "Detail Akun Monex",
                            listOf(
                                "Nama Tampilan" to displayName,
                                "Username" to username,
                                "Kode Referral" to referralCode
                            )
                        )
                    }
                )
                HorizontalDivider(color = MonexGlassBorder, modifier = Modifier.padding(horizontal = 14.dp))
                SettingsMenuItem(
                    icon = Icons.Rounded.Description,
                    title = "Dokumen & laporan",
                    onClick = {
                        onOpenInfoSheet(
                            "Dokumen & Laporan",
                            listOf(
                                "Mutasi Rekening" to "Seluruh transaksi keluar dan masuk tercatat langsung pada tab Beranda dan Analitik.",
                                "Laporan Anggaran" to "Atur batas anggaran bulanan kapan saja melalui kartu Anggaran di Analitik."
                            )
                        )
                    }
                )
                HorizontalDivider(color = MonexGlassBorder, modifier = Modifier.padding(horizontal = 14.dp))
                SettingsMenuItem(
                    icon = Icons.Rounded.Lightbulb,
                    title = "Belajar",
                    onClick = {
                        onOpenInfoSheet(
                            "Edukasi Keuangan Monex",
                            listOf(
                                "Aturan 50/30/20" to "Alokasikan 50% kebutuhan pokok, 30% gaya hidup, dan 20% tabungan atau investasi.",
                                "Diversifikasi Aset" to "Pantau aset kripto seperti BTC dan ETH secara terpisah dari dana darurat harian."
                            )
                        )
                    }
                )
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonexPink.copy(alpha = 0.14f),
                    contentColor = MonexPink
                ),
                border = BorderStroke(1.dp, MonexPink.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Keluar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Keluar dari Akun",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MonexTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MonexTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MonexTextMuted
        )
    }
}
