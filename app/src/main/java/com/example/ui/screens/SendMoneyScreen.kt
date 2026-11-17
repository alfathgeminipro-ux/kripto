package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransferContact
import com.example.data.UiState
import com.example.data.UserWallet
import com.example.ui.SendSuccessEvent
import com.example.ui.components.AvatarView
import com.example.ui.components.GlassCard
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.CURRENCY_SYMBOL
import com.example.util.formatKeypadInputRupiah
import com.example.util.formatRupiah

@Composable
fun SendMoneyScreen(
    walletState: UiState<UserWallet?>,
    contactsState: UiState<List<TransferContact>>,
    isProcessing: Boolean,
    sendSuccess: SendSuccessEvent?,
    onBack: () -> Unit,
    onAddNewContact: () -> Unit,
    onSendMoney: (recipientName: String, avatarKey: String, amountIdr: Long, note: String) -> Unit,
    onDoneAfterSuccess: () -> Unit
) {
    BackHandler(onBack = onBack)

    val wallet = (walletState as? UiState.Success)?.data
    val contacts = (contactsState as? UiState.Success)?.data.orEmpty()

    var selectedContactId by remember(contacts) {
        mutableStateOf(contacts.firstOrNull()?.id ?: "")
    }
    val selectedContact = contacts.firstOrNull { it.id == selectedContactId } ?: contacts.firstOrNull()

    var rawDigits by remember { mutableStateOf("250000") }
    val amountIdr = rawDigits.toLongOrNull() ?: 0L
    val currentBalance = wallet?.balanceIdr ?: 0L
    val insufficientBalance = amountIdr > currentBalance

    val cursorTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    if (sendSuccess != null) {
        SendSuccessCelebration(
            event = sendSuccess,
            remainingBalanceIdr = currentBalance,
            onDone = onDoneAfterSuccess
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, MonexGlassBorder, CircleShape)
                    .testTag("send_money_back_button")
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MonexTextPrimary
                )
            }
            Text(
                text = "Kirim Uang",
                style = MaterialTheme.typography.titleLarge,
                color = MonexTextPrimary
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Contacts Carousel (+ Tambah Baru + Contacts)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_new_contact_button")
                        .clickable(onClick = onAddNewContact)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MonexSurfaceElevated)
                            .border(1.dp, MonexGlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah kontak baru",
                            tint = MonexNeonLime
                        )
                    }
                    Text(
                        text = "Tambah Baru",
                        style = MaterialTheme.typography.labelSmall,
                        color = MonexTextSecondary
                    )
                }
            }

            items(contacts, key = { it.id }) { contact ->
                val isSelected = contact.id == selectedContact?.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { selectedContactId = contact.id }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MonexNeonLime else MonexGlassBorder,
                                shape = CircleShape
                            )
                            .padding(2.dp)
                    ) {
                        AvatarView(
                            avatarKey = contact.avatarKey,
                            fallbackName = contact.name,
                            accentHex = contact.accentHex,
                            size = 52.dp
                        )
                    }
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MonexNeonLime else MonexTextSecondary
                    )
                }
            }
        }

        // Glass Amount Card with blinking cursor & rolling digits
        GlassCard(
            accentGlow = !insufficientBalance,
            contentPadding = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MonexSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MonexGlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = MonexNeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "•••• ${wallet?.cardLast4 ?: "3377"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$CURRENCY_SYMBOL ",
                        style = MaterialTheme.typography.headlineMedium.copy(color = MonexTextSecondary)
                    )
                    AnimatedContent(
                        targetState = formatKeypadInputRupiah(rawDigits),
                        transitionSpec = {
                            (fadeIn(tween(130)) + slideInVertically { -it / 4 }) togetherWith
                                (fadeOut(tween(90)) + slideOutVertically { it / 4 })
                        },
                        label = "amountRoll"
                    ) { formatted ->
                        Text(
                            text = formatted,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (insufficientBalance) MonexPink else MonexTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(34.dp)
                            .alpha(cursorAlpha)
                            .background(MonexNeonLime)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (insufficientBalance) {
                        "Saldo tidak mencukupi (Tersedia ${formatRupiah(currentBalance)})"
                    } else {
                        "Masukkan nominal • Saldo ${formatRupiah(currentBalance)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (insufficientBalance) MonexPink else MonexTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick Amount Chips
        val quickChips = listOf(
            "Rp 50rb" to "50000",
            "Rp 100rb" to "100000",
            "Rp 500rb" to "500000",
            "Rp 1jt" to "1000000",
            "Rp 1,5jt" to "1500000"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { (label, valueStr) ->
                val active = rawDigits == valueStr
                Surface(
                    color = if (active) MonexNeonLime.copy(alpha = 0.2f) else Color(0x18FFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (active) MonexNeonLime else MonexGlassBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { rawDigits = valueStr }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) MonexNeonLime else MonexTextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Glass Numeric Keypad (0-9, 000, Backspace)
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("000", "0", "DEL")
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        KeypadGlassKey(
                            key = key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                rawDigits = when (key) {
                                    "DEL" -> if (rawDigits.length <= 1) "0" else rawDigits.dropLast(1)
                                    "000" -> if (rawDigits == "0") "0" else (rawDigits + "000").take(11)
                                    else -> if (rawDigits == "0") key else (rawDigits + key).take(11)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Primary Neon Lime Send Button
        Button(
            onClick = {
                val targetName = selectedContact?.name ?: "Penerima"
                val avatarKey = selectedContact?.avatarKey ?: "transfer"
                onSendMoney(targetName, avatarKey, amountIdr, "Transfer instan Monex")
            },
            enabled = amountIdr > 0 && !insufficientBalance && !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = MonexNeonLime,
                contentColor = MonexOnNeon,
                disabledContainerColor = Color(0x28C6FF1A),
                disabledContentColor = MonexTextSecondary
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("confirm_send_money_button")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MonexOnNeon,
                    strokeWidth = 2.4.dp
                )
            } else {
                Text(
                    text = "Kirim uang ke ${selectedContact?.name ?: "Penerima"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun KeypadGlassKey(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (pressed) Color(0x36C6FF1A) else Color(0x18FFFFFF))
            .border(
                width = 1.dp,
                color = if (pressed) MonexNeonLime.copy(alpha = 0.7f) else MonexGlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Hapus digit",
                tint = MonexTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = key,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MonexTextPrimary
            )
        }
    }
}

@Composable
private fun SendSuccessCelebration(
    event: SendSuccessEvent,
    remainingBalanceIdr: Long,
    onDone: () -> Unit
) {
    val checkProgress = remember { Animatable(0f) }
    val flyProgress = remember { Animatable(0f) }

    LaunchedEffect(event) {
        checkProgress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        flyProgress.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            accentGlow = true,
            contentPadding = 26.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MonexNeonLime),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(38.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.20f, size.height * 0.52f)
                            lineTo(size.width * 0.42f, size.height * 0.74f)
                            lineTo(size.width * 0.80f, size.height * 0.28f)
                        }
                        val measure = PathMeasure()
                        measure.setPath(path, false)
                        val seg = Path()
                        measure.getSegment(0f, measure.length * checkProgress.value, seg, true)
                        drawPath(
                            path = seg,
                            color = MonexOnNeon,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                Text(
                    text = "Transfer Berhasil!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MonexTextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.graphicsLayer {
                        translationY = (1f - flyProgress.value) * 18f
                        alpha = 0.4f + 0.6f * flyProgress.value
                    }
                ) {
                    AvatarView(
                        avatarKey = event.avatarKey,
                        fallbackName = event.recipientName,
                        size = 40.dp
                    )
                    Text(
                        text = "${formatRupiah(event.amountIdr)} ke ${event.recipientName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MonexNeonLime
                    )
                }

                Text(
                    text = "Sisa saldo dompet: ${formatRupiah(remainingBalanceIdr)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonexTextSecondary,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = MonexNeonLime, contentColor = MonexOnNeon),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_success_done_button")
                ) {
                    Text(
                        text = "Kembali ke Beranda",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
