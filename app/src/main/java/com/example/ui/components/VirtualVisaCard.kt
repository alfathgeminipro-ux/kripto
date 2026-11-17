package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserWallet
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon

@Composable
fun VirtualVisaCardSection(
    wallet: UserWallet,
    onCopied: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(false) }
    var dragAccum by remember { mutableFloatStateOf(0f) }
    var sheenShift by remember { mutableFloatStateOf(0.35f) }

    val cardHeight by animateDpAsState(
        targetValue = if (expanded) 196.dp else 44.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "visaPullHeight"
    )

    val flipRotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "visaFlip"
    )

    val glossyBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB7FF1E),
            Color(0xFFE7FF9E),
            Color(0xFFC6FF1A),
            Color(0xFF93DC00)
        ),
        start = Offset(sheenShift * 420f, 0f),
        end = Offset(sheenShift * 420f + 520f, 440f)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pull-down / Expandable VISA Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .pointerInput(expanded) {
                    detectDragGestures(
                        onDragEnd = { dragAccum = 0f }
                    ) { change, dragAmount ->
                        change.consume()
                        dragAccum += dragAmount.y
                        sheenShift = (sheenShift + dragAmount.x * 0.002f).coerceIn(0f, 1f)
                        if (!expanded && dragAccum > 28f) {
                            expanded = true
                            dragAccum = 0f
                        } else if (expanded && dragAccum < -28f) {
                            expanded = false
                            flipped = false
                            dragAccum = 0f
                        }
                    }
                }
                .graphicsLayer {
                    rotationY = if (expanded) flipRotation else 0f
                    cameraDistance = 14f * density
                }
                .clip(RoundedCornerShape(24.dp))
                .background(glossyBrush)
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(24.dp))
                .testTag("virtual_visa_card")
                .clickable {
                    if (!expanded) {
                        expanded = true
                    } else {
                        flipped = !flipped
                    }
                }
                .padding(horizontal = 20.dp, vertical = if (expanded) 18.dp else 10.dp)
        ) {
            if (!expanded) {
                // Collapsed Peek Strip
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${wallet.planName}  •• ${wallet.cardLast4}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MonexOnNeon
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Tarik kartu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexOnNeon.copy(alpha = 0.78f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Buka kartu VISA",
                            tint = MonexOnNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (flipRotation <= 90f) {
                // Front Face of VISA Card
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wallet.planName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MonexOnNeon
                        )
                        Text(
                            text = "Ketuk untuk balik",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexOnNeon.copy(alpha = 0.7f)
                        )
                    }

                    // Subtle metallic chip visual
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0x33070B14))
                            .border(1.dp, Color(0x55070B14), RoundedCornerShape(7.dp))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "•• ${wallet.cardLast4}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MonexOnNeon
                        )
                        Text(
                            text = "VISA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                letterSpacing = 1.sp
                            ),
                            color = MonexOnNeon
                        )
                    }
                }
            } else {
                // Back Face (un-mirrored via inner rotationY = 180f)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kartu Virtual Dummy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexOnNeon.copy(alpha = 0.72f)
                        )
                        Text(
                            text = "Ketuk untuk balik",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexOnNeon.copy(alpha = 0.72f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Nomor kartu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexOnNeon.copy(alpha = 0.68f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = wallet.cardNumberMasked,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MonexOnNeon
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1E070B14))
                                    .testTag("copy_card_number_button")
                                    .clickable {
                                        copyToClipboard(context, "Nomor Kartu Monex", wallet.cardNumberMasked)
                                        onCopied("Nomor kartu virtual disalin.")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Salin nomor kartu",
                                    tint = MonexOnNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kedaluwarsa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MonexOnNeon.copy(alpha = 0.68f)
                            )
                            Text(
                                text = wallet.cardExpiry,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MonexOnNeon
                            )
                        }
                        Column {
                            Text(
                                text = "CVV",
                                style = MaterialTheme.typography.labelSmall,
                                color = MonexOnNeon.copy(alpha = 0.68f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = wallet.cardCvv,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MonexOnNeon
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1E070B14))
                                        .testTag("copy_card_cvv_button")
                                        .clickable {
                                            copyToClipboard(context, "CVV Monex", wallet.cardCvv)
                                            onCopied("CVV kartu disalin.")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Salin CVV",
                                        tint = MonexOnNeon,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collapse / Expand handle chevron below card
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("collapse_visa_card_button")
                    .clickable {
                        expanded = false
                        flipped = false
                    }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Tutup kartu VISA",
                    tint = MonexNeonLime,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
}
