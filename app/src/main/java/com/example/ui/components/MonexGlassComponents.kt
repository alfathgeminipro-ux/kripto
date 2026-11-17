package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MonexTab
import com.example.ui.theme.MonexBlueOrb
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexGlassHighlight
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexPurpleOrb
import com.example.ui.theme.MonexSurface
import com.example.ui.theme.MonexSurfaceElevated
import com.example.ui.theme.MonexTextMuted
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import com.example.util.formatRupiah
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedOrbBackground(
    modifier: Modifier = Modifier,
    parallaxOffset: Offset = Offset.Zero
) {
    val transition = rememberInfiniteTransition(label = "orbs")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val greenCenter = Offset(
            x = w * 0.18f + cos(phase) * (w * 0.08f) + parallaxOffset.x,
            y = h * 0.16f + sin(phase) * (h * 0.06f) + parallaxOffset.y
        )
        val blueCenter = Offset(
            x = w * 0.85f + sin(phase * 0.8f) * (w * 0.09f) - parallaxOffset.x * 0.8f,
            y = h * 0.42f + cos(phase * 0.9f) * (h * 0.08f) - parallaxOffset.y * 0.8f
        )
        val purpleCenter = Offset(
            x = w * 0.35f + sin(phase * 1.1f) * (w * 0.10f) + parallaxOffset.x * 0.5f,
            y = h * 0.82f + cos(phase * 0.7f) * (h * 0.07f) + parallaxOffset.y * 0.5f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MonexNeonLime.copy(alpha = 0.22f), Color.Transparent),
                center = greenCenter,
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = greenCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MonexBlueOrb.copy(alpha = 0.22f), Color.Transparent),
                center = blueCenter,
                radius = w * 0.60f
            ),
            radius = w * 0.60f,
            center = blueCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MonexPurpleOrb.copy(alpha = 0.20f), Color.Transparent),
                center = purpleCenter,
                radius = w * 0.62f
            ),
            radius = w * 0.62f,
            center = purpleCenter
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    enableTilt: Boolean = false,
    accentGlow: Boolean = false,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    val animTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tiltX"
    )
    val animTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tiltY"
    )

    val tiltModifier = if (enableTilt) {
        Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    tiltX = 0f
                    tiltY = 0f
                },
                onDragCancel = {
                    tiltX = 0f
                    tiltY = 0f
                }
            ) { change, dragAmount ->
                change.consume()
                tiltY = (tiltY + dragAmount.x * 0.08f).coerceIn(-7f, 7f)
                tiltX = (tiltX - dragAmount.y * 0.08f).coerceIn(-7f, 7f)
            }
        }
    } else Modifier

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (accentGlow) MonexNeonLime.copy(alpha = 0.45f) else MonexGlassHighlight,
            MonexGlassBorder,
            Color(0x10FFFFFF)
        )
    )

    val bgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x20FFFFFF),
            MonexSurface.copy(alpha = 0.78f),
            Color(0x14FFFFFF)
        ),
        start = Offset.Zero,
        end = Offset(700f, 700f)
    )

    Column(
        modifier = modifier
            .then(tiltModifier)
            .graphicsLayer {
                rotationX = animTiltX
                rotationY = animTiltY
                cameraDistance = 14f * density
            }
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun AnimatedCountUpRupiah(
    targetAmountIdr: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MonexTextPrimary
) {
    val animatedAmount = remember { Animatable(0f) }
    LaunchedEffect(targetAmountIdr) {
        animatedAmount.animateTo(
            targetValue = targetAmountIdr.toFloat(),
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = formatRupiah(animatedAmount.value.toLong()),
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun AvatarView(
    avatarKey: String,
    photoUrl: String = "",
    fallbackName: String = "M",
    accentHex: String = "#C6FF1A",
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val bundledRes = when (avatarKey.lowercase()) {
        "james" -> R.drawable.img_avatar_james_1794925559114
        "marie" -> R.drawable.img_avatar_marie_1794925558306
        "john" -> R.drawable.img_avatar_john_1794925559634
        else -> null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MonexSurfaceElevated)
            .border(1.dp, MonexGlassBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            photoUrl.isNotBlank() -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = fallbackName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            bundledRes != null -> {
                Image(
                    painter = painterResource(id = bundledRes),
                    contentDescription = fallbackName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(accentHex))
                } catch (_: Exception) {
                    MonexNeonLime
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(parsedColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fallbackName.trim().take(1).uppercase().ifEmpty { "M" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = parsedColor
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionIconBadge(
    iconKey: String,
    modifier: Modifier = Modifier
) {
    when (iconKey.lowercase()) {
        "james", "marie", "john" -> {
            AvatarView(avatarKey = iconKey, fallbackName = iconKey, size = 44.dp, modifier = modifier)
        }
        else -> {
            val (icon, tint, bg) = when (iconKey.lowercase()) {
                "bolt" -> Triple(Icons.Rounded.Fastfood, MonexNeonLime, Color(0x28C6FF1A))
                "uber" -> Triple(Icons.Rounded.DirectionsCar, MonexTextPrimary, Color(0x24FFFFFF))
                "topup" -> Triple(Icons.Rounded.ArrowDownward, MonexNeonLime, Color(0x28C6FF1A))
                "withdraw" -> Triple(Icons.Rounded.ArrowUpward, MonexPink, Color(0x28FF4D88))
                "exchange" -> Triple(Icons.Rounded.SwapHoriz, MonexBlueOrb, Color(0x282E7CF6))
                "btc", "eth", "sol", "ada" -> Triple(Icons.Rounded.CurrencyBitcoin, MonexNeonLime, Color(0x28C6FF1A))
                else -> Triple(Icons.AutoMirrored.Rounded.Send, MonexNeonLime, Color(0x28C6FF1A))
            }
            Box(
                modifier = modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, MonexGlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconKey,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SkeletonShimmerCard(
    height: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x12FFFFFF),
                        Color(0x26FFFFFF),
                        Color(0x12FFFFFF)
                    ),
                    start = Offset(shift, 0f),
                    end = Offset(shift + 300f, 180f)
                )
            )
            .border(1.dp, MonexGlassBorder, RoundedCornerShape(24.dp))
    )
}

@Composable
fun FloatingGlassNavBar(
    selectedTab: MonexTab,
    onSelectTab: (MonexTab) -> Unit,
    onOpenSendMoney: () -> Unit,
    onQuickTopUp: () -> Unit,
    onQuickExchange: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quickMenuOpen by remember { mutableStateOf(false) }

    val pulseTransition = rememberInfiniteTransition(label = "fabPulse")
    val haloScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expanding Quick Actions Menu above "+" button
        AnimatedVisibility(
            visible = quickMenuOpen,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.78f, animationSpec = spring(dampingRatio = 0.7f)),
            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.78f)
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MonexSurface.copy(alpha = 0.94f))
                    .border(1.dp, MonexNeonLime.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickMenuPill(
                    icon = Icons.AutoMirrored.Rounded.Send,
                    label = "Kirim",
                    tag = "quick_action_send",
                    onClick = {
                        quickMenuOpen = false
                        onOpenSendMoney()
                    }
                )
                QuickMenuPill(
                    icon = Icons.Rounded.ArrowDownward,
                    label = "Isi Saldo",
                    tag = "quick_action_topup",
                    onClick = {
                        quickMenuOpen = false
                        onQuickTopUp()
                    }
                )
                QuickMenuPill(
                    icon = Icons.Rounded.SwapHoriz,
                    label = "Tukar",
                    tag = "quick_action_exchange",
                    onClick = {
                        quickMenuOpen = false
                        onQuickExchange()
                    }
                )
            }
        }

        // Main Floating Glass Dock
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x26FFFFFF),
                            MonexSurface.copy(alpha = 0.88f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(MonexGlassHighlight, MonexGlassBorder)
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val slotWidth = maxWidth / 5f
            val tabIndex = when (selectedTab) {
                MonexTab.HOME -> 0
                MonexTab.ANALYTICS -> 1
                MonexTab.CRYPTO -> 3
                MonexTab.SETTINGS -> 4
            }
            val indicatorOffset by animateDpAsState(
                targetValue = slotWidth * tabIndex,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium),
                label = "navPillOffset"
            )

            // Sliding active glass pill
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = indicatorOffset + 4.dp)
                    .width(slotWidth - 8.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0x1EFFFFFF))
                    .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(26.dp))
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavDockItem(
                    tab = MonexTab.HOME,
                    icon = Icons.Rounded.Home,
                    selected = selectedTab == MonexTab.HOME,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        quickMenuOpen = false
                        onSelectTab(MonexTab.HOME)
                    }
                )
                NavDockItem(
                    tab = MonexTab.ANALYTICS,
                    icon = Icons.Rounded.BarChart,
                    selected = selectedTab == MonexTab.ANALYTICS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        quickMenuOpen = false
                        onSelectTab(MonexTab.ANALYTICS)
                    }
                )

                // Center pulsing "+" Neon FAB
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .graphicsLayer {
                                scaleX = haloScale
                                scaleY = haloScale
                            }
                            .clip(CircleShape)
                            .background(MonexNeonLime.copy(alpha = 0.22f))
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(MonexNeonLime)
                            .testTag("nav_center_plus_button")
                            .clickable { quickMenuOpen = !quickMenuOpen },
                        contentAlignment = Alignment.Center
                    ) {
                        val rot by animateFloatAsState(
                            targetValue = if (quickMenuOpen) 45f else 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "plusRotate"
                        )
                        Icon(
                            imageVector = if (quickMenuOpen) Icons.Rounded.Close else Icons.Rounded.Add,
                            contentDescription = "Menu aksi cepat",
                            tint = MonexOnNeon,
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer { rotationZ = rot }
                        )
                    }
                }

                NavDockItem(
                    tab = MonexTab.CRYPTO,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    selected = selectedTab == MonexTab.CRYPTO,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        quickMenuOpen = false
                        onSelectTab(MonexTab.CRYPTO)
                    }
                )
                NavDockItem(
                    tab = MonexTab.SETTINGS,
                    icon = Icons.Rounded.Settings,
                    selected = selectedTab == MonexTab.SETTINGS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        quickMenuOpen = false
                        onSelectTab(MonexTab.SETTINGS)
                    }
                )
            }
        }
    }
}

@Composable
private fun NavDockItem(
    tab: MonexTab,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconHop by animateDpAsState(
        targetValue = if (selected) (-2).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "iconHop"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .minimumInteractiveComponentSize()
            .testTag("nav_tab_${tab.name.lowercase()}")
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.label,
            tint = if (selected) MonexNeonLime else MonexTextMuted,
            modifier = Modifier
                .size(21.dp)
                .offset(y = iconHop)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MonexTextPrimary else MonexTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickMenuPill(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0x1EFFFFFF),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MonexGlassBorder),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MonexNeonLime,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MonexTextPrimary
            )
        }
    }
}

@Composable
fun MonexNavigationRail(
    selectedTab: MonexTab,
    onSelectTab: (MonexTab) -> Unit,
    onOpenSendMoney: () -> Unit
) {
    NavigationRail(
        containerColor = MonexSurface.copy(alpha = 0.85f),
        contentColor = MonexTextPrimary,
        header = {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(MonexNeonLime)
                    .testTag("rail_send_button")
                    .clickable(onClick = onOpenSendMoney),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Kirim Uang",
                    tint = MonexOnNeon
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    ) {
        val items = listOf(
            MonexTab.HOME to Icons.Rounded.Home,
            MonexTab.ANALYTICS to Icons.Rounded.BarChart,
            MonexTab.CRYPTO to Icons.Rounded.AccountBalanceWallet,
            MonexTab.SETTINGS to Icons.Rounded.Settings
        )
        items.forEach { (tab, icon) ->
            NavigationRailItem(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                icon = { Icon(imageVector = icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MonexNeonLime,
                    selectedTextColor = MonexTextPrimary,
                    indicatorColor = Color(0x22FFFFFF),
                    unselectedIconColor = MonexTextMuted,
                    unselectedTextColor = MonexTextMuted
                )
            )
        }
    }
}
