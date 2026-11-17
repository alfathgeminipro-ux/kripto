package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CryptoHolding
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurface
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.formatRupiah
import java.util.Locale

enum class WalletActionSheetType {
    TOP_UP,
    WITHDRAW,
    EXCHANGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletActionBottomSheet(
    type: WalletActionSheetType,
    currentBalanceIdr: Long,
    onDismiss: () -> Unit,
    onConfirmTopUp: (Long, String) -> Unit,
    onConfirmWithdraw: (Long, String) -> Unit,
    onConfirmExchange: (Long, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf("500000") }
    var selectedTarget by remember {
        mutableStateOf(
            when (type) {
                WalletActionSheetType.TOP_UP -> "BCA Virtual Account"
                WalletActionSheetType.WITHDRAW -> "Bank Mandiri"
                WalletActionSheetType.EXCHANGE -> "USD"
            }
        )
    }

    val options = when (type) {
        WalletActionSheetType.TOP_UP -> listOf("BCA Virtual Account", "Mandiri VA", "Kartu Debit")
        WalletActionSheetType.WITHDRAW -> listOf("Bank Mandiri", "Bank BCA", "BRI")
        WalletActionSheetType.EXCHANGE -> listOf("USD", "EUR", "SGD")
    }

    val presets = listOf(250_000L, 500_000L, 1_000_000L, 2_500_000L)
    val parsedAmount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val conversionPreview = if (type == WalletActionSheetType.EXCHANGE && parsedAmount > 0) {
        val rate = when (selectedTarget) {
            "USD" -> 15_850.0
            "EUR" -> 17_200.0
            else -> 11_800.0
        }
        val foreign = parsedAmount.toDouble() / rate
        val sym = when (selectedTarget) {
            "USD" -> "$"
            "EUR" -> "€"
            else -> "S$"
        }
        "$sym${String.format(Locale.US, "%.2f", foreign)}"
    } else ""

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MonexSurface,
        contentColor = MonexTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val title = when (type) {
                WalletActionSheetType.TOP_UP -> "Isi Saldo Dompet"
                WalletActionSheetType.WITHDRAW -> "Tarik Dana ke Rekening"
                WalletActionSheetType.EXCHANGE -> "Tukar ke Mata Uang Asing"
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Saldo tersedia: ${formatRupiah(currentBalanceIdr)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MonexTextSecondary
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { input -> amountText = input.filter { it.isDigit() }.take(12) },
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonexNeonLime,
                    unfocusedBorderColor = MonexGlassBorder,
                    focusedTextColor = MonexTextPrimary,
                    unfocusedTextColor = MonexTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_sheet_amount_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val label = if (preset >= 1_000_000L) "${preset / 1_000_000.0}jt".replace(".0", "") else "${preset / 1_000}rb"
                    Surface(
                        color = MonexSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MonexGlassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { amountText = preset.toString() }
                    ) {
                        Text(
                            text = "Rp $label",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexTextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }
                }
            }

            Text(
                text = when (type) {
                    WalletActionSheetType.TOP_UP -> "Pilih metode isi saldo"
                    WalletActionSheetType.WITHDRAW -> "Pilih bank tujuan"
                    WalletActionSheetType.EXCHANGE -> "Pilih mata uang tujuan"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MonexTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { opt ->
                    FilterChip(
                        selected = selectedTarget == opt,
                        onClick = { selectedTarget = opt },
                        label = { Text(opt, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MonexNeonLime.copy(alpha = 0.2f),
                            selectedLabelColor = MonexNeonLime,
                            containerColor = MonexSurfaceElevated,
                            labelColor = MonexTextSecondary
                        )
                    )
                }
            }

            if (conversionPreview.isNotBlank()) {
                Surface(
                    color = Color(0x1AC6FF1A),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MonexNeonLime.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Estimasi diterima: $conversionPreview",
                        style = MaterialTheme.typography.labelLarge,
                        color = MonexNeonLime,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (parsedAmount > 0) {
                        when (type) {
                            WalletActionSheetType.TOP_UP -> onConfirmTopUp(parsedAmount, selectedTarget)
                            WalletActionSheetType.WITHDRAW -> onConfirmWithdraw(parsedAmount, selectedTarget)
                            WalletActionSheetType.EXCHANGE -> onConfirmExchange(parsedAmount, selectedTarget, conversionPreview)
                        }
                        onDismiss()
                    }
                },
                enabled = parsedAmount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonexNeonLime,
                    contentColor = MonexOnNeon
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("wallet_sheet_confirm_button")
            ) {
                Text(
                    text = when (type) {
                        WalletActionSheetType.TOP_UP -> "Konfirmasi Isi Saldo"
                        WalletActionSheetType.WITHDRAW -> "Tarik Dana Sekarang"
                        WalletActionSheetType.EXCHANGE -> "Tukar Sekarang"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTradeBottomSheet(
    holdings: List<CryptoHolding>,
    initialActionType: String,
    initialSymbol: String,
    currentBalanceIdr: Long,
    onDismiss: () -> Unit,
    onConfirmTrade: (symbol: String, actionType: String, amountIdr: Long) -> Unit
) {
    var selectedAction by remember { mutableStateOf(initialActionType) }
    var selectedSymbol by remember { mutableStateOf(initialSymbol) }
    var amountText by remember { mutableStateOf("1000000") }
    val parsedAmount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val actions = listOf(
        "crypto_buy" to "Beli",
        "crypto_sell" to "Jual",
        "crypto_send" to "Kirim",
        "crypto_receive" to "Terima"
    )
    val selectedHolding = holdings.firstOrNull { it.symbol == selectedSymbol } ?: holdings.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MonexSurface,
        contentColor = MonexTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "Transaksi Kripto", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Saldo dompet: ${formatRupiah(currentBalanceIdr)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MonexTextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { (code, label) ->
                    FilterChip(
                        selected = selectedAction == code,
                        onClick = { selectedAction = code },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MonexNeonLime.copy(alpha = 0.2f),
                            selectedLabelColor = MonexNeonLime,
                            containerColor = MonexSurfaceElevated,
                            labelColor = MonexTextSecondary
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                holdings.forEach { h ->
                    FilterChip(
                        selected = selectedSymbol == h.symbol,
                        onClick = { selectedSymbol = h.symbol },
                        label = { Text(h.symbol, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MonexNeonLime.copy(alpha = 0.2f),
                            selectedLabelColor = MonexNeonLime,
                            containerColor = MonexSurfaceElevated,
                            labelColor = MonexTextSecondary
                        )
                    )
                }
            }

            if (selectedHolding != null) {
                val units = if (selectedHolding.priceIdr > 0) {
                    parsedAmount.toDouble() / selectedHolding.priceIdr.toDouble()
                } else 0.0
                Text(
                    text = "Aset dimiliki: ${String.format(Locale.US, "%.4f", selectedHolding.amountUnits)} ${selectedHolding.symbol} • Estimasi: ${String.format(Locale.US, "%.5f", units)} ${selectedHolding.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonexTextSecondary
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { input -> amountText = input.filter { it.isDigit() }.take(12) },
                label = { Text("Nominal dalam Rupiah (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonexNeonLime,
                    unfocusedBorderColor = MonexGlassBorder,
                    focusedTextColor = MonexTextPrimary,
                    unfocusedTextColor = MonexTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (parsedAmount > 0 && selectedSymbol.isNotBlank()) {
                        onConfirmTrade(selectedSymbol, selectedAction, parsedAmount)
                        onDismiss()
                    }
                },
                enabled = parsedAmount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonexNeonLime,
                    contentColor = MonexOnNeon
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("crypto_sheet_confirm_button")
            ) {
                Text(
                    text = "Proses $selectedSymbol",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactBottomSheet(
    onDismiss: () -> Unit,
    onConfirmAdd: (name: String, handle: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MonexSurface,
        contentColor = MonexTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Tambah Penerima Baru", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                label = { Text("Nama lengkap") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonexNeonLime,
                    unfocusedBorderColor = MonexGlassBorder,
                    focusedTextColor = MonexTextPrimary,
                    unfocusedTextColor = MonexTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it.take(30) },
                label = { Text("Username (@contoh)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonexNeonLime,
                    unfocusedBorderColor = MonexGlassBorder,
                    focusedTextColor = MonexTextPrimary,
                    unfocusedTextColor = MonexTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirmAdd(name, handle)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MonexNeonLime, contentColor = MonexOnNeon),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Simpan Kontak", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditBottomSheet(
    currentBudgetIdr: Long,
    onDismiss: () -> Unit,
    onConfirmBudget: (Long) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudgetIdr.toString()) }
    val parsed = budgetText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MonexSurface,
        contentColor = MonexTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Atur Anggaran Bulanan", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Sesuaikan batas pengeluaran bulanan untuk memantau rasio anggaran.",
                style = MaterialTheme.typography.bodyMedium,
                color = MonexTextSecondary
            )
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it.filter { ch -> ch.isDigit() }.take(12) },
                label = { Text("Batas Anggaran (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonexNeonLime,
                    unfocusedBorderColor = MonexGlassBorder,
                    focusedTextColor = MonexTextPrimary,
                    unfocusedTextColor = MonexTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (parsed >= 100_000L) {
                        onConfirmBudget(parsed)
                        onDismiss()
                    }
                },
                enabled = parsed >= 100_000L,
                colors = ButtonDefaults.buttonColors(containerColor = MonexNeonLime, contentColor = MonexOnNeon),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Simpan Anggaran", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            if (parsed in 1L..99_999L) {
                Text(
                    text = "Minimal anggaran Rp 100.000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonexPink
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDetailBottomSheet(
    title: String,
    bullets: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MonexSurface,
        contentColor = MonexTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            bullets.forEach { (head, desc) ->
                Surface(
                    color = MonexSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MonexGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = head, style = MaterialTheme.typography.titleMedium, color = MonexNeonLime)
                        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = MonexTextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
