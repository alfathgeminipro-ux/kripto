package com.example.ui.auth

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
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.components.AnimatedOrbBackground
import com.example.ui.theme.MonexBg
import com.example.ui.theme.MonexGlassBorder
import com.example.ui.theme.MonexNeonLime
import com.example.ui.theme.MonexOnNeon
import com.example.ui.theme.MonexPink
import com.example.ui.theme.MonexSurface
import com.example.ui.theme.MonexTextPrimary
import com.example.ui.theme.MonexTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class LoginAnimPhase {
    IDLE,
    LOADING,
    SUCCESS_CHECK,
    CIRCULAR_REVEAL
}

@Composable
fun AuthScreen(
    onAuthCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var phase by remember { mutableStateOf(LoginAnimPhase.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cardEntered by remember { mutableStateOf(false) }
    var pointerParallax by remember { mutableStateOf(Offset.Zero) }

    // Card entrance animations (blur-to-sharp + slide up)
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardEntered) 1f else 0f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (cardEntered) 0.dp else 44.dp,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "cardOffsetY"
    )
    val cardBlur by animateDpAsState(
        targetValue = if (cardEntered) 0.dp else 18.dp,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "cardBlur"
    )

    // Shake animation on error
    val shakeX = remember { Animatable(0f) }
    val errorBorderFlash = remember { Animatable(0f) }

    // Morphing Google button width / progress
    val buttonScale = remember { Animatable(1f) }
    val checkProgress = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }
    val revealProgress = remember { Animatable(0f) }

    // Rotating border sheen
    val infiniteTransition = rememberInfiniteTransition(label = "authSheen")
    val sheenAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenAngle"
    )

    LaunchedEffect(Unit) {
        cardEntered = true
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            errorBorderFlash.snapTo(1f)
            launch { errorBorderFlash.animateTo(0f, tween(700)) }
            val offsets = listOf(-18f, 16f, -12f, 10f, -5f, 0f)
            for (x in offsets) {
                shakeX.animateTo(x, tween(45))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonexBg)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position
                        if (pos != null && size.width > 0 && size.height > 0) {
                            val nx = (pos.x / size.width - 0.5f) * 28f
                            val ny = (pos.y / size.height - 0.5f) * 28f
                            pointerParallax = Offset(nx, ny)
                        }
                    }
                }
            }
    ) {
        AnimatedOrbBackground(parallaxOffset = pointerParallax)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .graphicsLayer { alpha = cardAlpha }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MonexNeonLime, Color(0xFF91D900))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MonexOnNeon,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = "Monex",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MonexTextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Rumah modern untuk keuangan harian Anda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonexTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Center Glass Auth Card
            val borderColor = if (errorBorderFlash.value > 0.05f) {
                MonexPink.copy(alpha = 0.85f * errorBorderFlash.value + 0.15f)
            } else {
                MonexGlassBorder
            }

            val rad = Math.toRadians(sheenAngle.toDouble())
            val sheenStart = Offset(
                (0.5 + 0.5 * cos(rad)).toFloat() * 900f,
                (0.5 + 0.5 * sin(rad)).toFloat() * 900f
            )

            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .offset { IntOffset(shakeX.value.roundToInt(), 0) }
                    .offset(y = cardOffsetY)
                    .blur(cardBlur)
                    .graphicsLayer {
                        alpha = cardAlpha
                        val shrink = if (phase == LoginAnimPhase.LOADING || phase == LoginAnimPhase.SUCCESS_CHECK) 0.97f else 1f
                        scaleX = shrink
                        scaleY = shrink
                    }
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MonexSurface.copy(alpha = 0.86f),
                                Color(0x20FFFFFF),
                                MonexSurface.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MonexNeonLime.copy(alpha = 0.55f),
                                borderColor,
                                Color(0x45FFFFFF),
                                borderColor
                            ),
                            start = sheenStart,
                            end = Offset(900f - sheenStart.x, 900f - sheenStart.y)
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Badge
                    Surface(
                        color = MonexNeonLime.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MonexNeonLime.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = MonexNeonLime,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Sinkronisasi Cloud Aman",
                                style = MaterialTheme.typography.labelSmall,
                                color = MonexNeonLime
                            )
                        }
                    }

                    Text(
                        text = "Masuk ke Dompet Monex",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MonexTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Kelola saldo Rupiah, kartu VISA virtual, transfer instan, analitik, dan portofolio kripto dalam satu tempat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonexTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Morphing Google Sign-In Button
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val pressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.96f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "btnPressScale"
                    )
                    val buttonWidthFraction by animateFloatAsState(
                        targetValue = if (phase == LoginAnimPhase.IDLE) 1f else 0.18f,
                        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                        label = "btnMorphWidth"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(buttonWidthFraction.coerceAtLeast(0.18f))
                            .widthIn(min = 56.dp)
                            .height(54.dp)
                            .graphicsLayer {
                                scaleX = pressScale * buttonScale.value
                                scaleY = pressScale * buttonScale.value
                            }
                            .clip(RoundedCornerShape(27.dp))
                            .background(
                                if (phase == LoginAnimPhase.SUCCESS_CHECK || phase == LoginAnimPhase.CIRCULAR_REVEAL) {
                                    Brush.linearGradient(listOf(MonexNeonLime, Color(0xFF9BE600)))
                                } else {
                                    Brush.linearGradient(
                                        listOf(Color(0x28FFFFFF), Color(0x16FFFFFF))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (phase == LoginAnimPhase.IDLE) Color(0x45FFFFFF) else MonexNeonLime,
                                shape = RoundedCornerShape(27.dp)
                            )
                            .testTag("google_sign_in_button")
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                enabled = phase == LoginAnimPhase.IDLE
                            ) {
                                errorMessage = null
                                phase = LoginAnimPhase.LOADING
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        scope.launch {
                                            phase = LoginAnimPhase.SUCCESS_CHECK
                                            launch {
                                                checkProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                                            }
                                            launch {
                                                particleProgress.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
                                            }
                                            delay(420)
                                            phase = LoginAnimPhase.CIRCULAR_REVEAL
                                            revealProgress.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
                                            onAuthCompleted()
                                        }
                                    },
                                    onAuthError = { msg ->
                                        phase = LoginAnimPhase.IDLE
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        phase = LoginAnimPhase.IDLE
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (phase) {
                            LoginAnimPhase.IDLE -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    GoogleLogoIcon(modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Lanjut dengan Google",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 15.sp,
                                            color = MonexTextPrimary
                                        )
                                    )
                                }
                            }
                            LoginAnimPhase.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = MonexNeonLime,
                                    strokeWidth = 2.6.dp
                                )
                            }
                            LoginAnimPhase.SUCCESS_CHECK, LoginAnimPhase.CIRCULAR_REVEAL -> {
                                Canvas(modifier = Modifier.size(34.dp)) {
                                    // Self-drawing checkmark
                                    val checkPath = Path().apply {
                                        moveTo(size.width * 0.24f, size.height * 0.52f)
                                        lineTo(size.width * 0.43f, size.height * 0.72f)
                                        lineTo(size.width * 0.78f, size.height * 0.30f)
                                    }
                                    val measure = PathMeasure()
                                    measure.setPath(checkPath, false)
                                    val segment = Path()
                                    measure.getSegment(0f, measure.length * checkProgress.value, segment, true)
                                    drawPath(
                                        path = segment,
                                        color = MonexOnNeon,
                                        style = Stroke(
                                            width = 4.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    // Particle burst around button
                                    val p = particleProgress.value
                                    if (p > 0f && p < 1f) {
                                        val count = 8
                                        for (i in 0 until count) {
                                            val angle = (i * (360f / count)) * Math.PI / 180.0
                                            val dist = size.minDimension * (0.65f + p * 0.9f)
                                            val cx = center.x + (cos(angle) * dist).toFloat()
                                            val cy = center.y + (sin(angle) * dist).toFloat()
                                            drawCircle(
                                                color = MonexNeonLime.copy(alpha = 1f - p),
                                                radius = 3.dp.toPx() * (1f - p * 0.5f),
                                                center = Offset(cx, cy)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Slide-down Indonesian error banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = MonexPink.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MonexPink.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFB2CC),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // Security footer inside card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.alpha(if (phase == LoginAnimPhase.IDLE) 0.78f else 0.25f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MonexTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Dilindungi oleh Firebase Auth & Cloud Firestore",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonexTextSecondary
                        )
                    }
                }
            }

            // Bottom dummy notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MonexTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Data saldo & kartu virtual bersifat simulasi aman",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonexTextSecondary
                )
            }
        }

        // Full-screen Neon Lime Circular Reveal Overlay on Auth Success
        if (phase == LoginAnimPhase.CIRCULAR_REVEAL) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadius = kotlin.math.hypot(size.width, size.height)
                drawCircle(
                    color = MonexNeonLime,
                    radius = maxRadius * revealProgress.value,
                    center = Offset(size.width / 2f, size.height * 0.56f)
                )
            }
        }
    }
}

@Composable
private fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.2f
        val inset = stroke / 2f
        val arcSize = Size(w - stroke, h - stroke)
        val topLeft = Offset(inset, inset)

        // Blue
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -35f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Green
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Yellow
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Red
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 210f,
            sweepAngle = 115f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Horizontal bar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.52f, h * 0.5f),
            end = Offset(w * 0.94f, h * 0.5f),
            strokeWidth = stroke * 0.95f,
            cap = StrokeCap.Round
        )
    }
}
