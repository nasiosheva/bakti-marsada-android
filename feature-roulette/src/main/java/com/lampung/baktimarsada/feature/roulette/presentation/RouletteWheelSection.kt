package com.lampung.baktimarsada.feature.roulette.presentation

import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.model.RoulettePendingSpin
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
internal fun RouletteWheelCard(
    names: List<String>,
    pendingSpin: RoulettePendingSpin?,
    onSpinRequested: (Float) -> Boolean,
    onSpinAnimationCompleted: (Long) -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val toneGenerator = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, TONE_VOLUME) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { toneGenerator?.release() }
    }

    LaunchedEffect(pendingSpin?.requestId) {
        val currentRequest = pendingSpin ?: return@LaunchedEffect
        val nameCount = names.size.coerceAtLeast(1)
        val sweepPerSlice = 360f / nameCount
        var lastTickedSlice = floor(rotation.value / sweepPerSlice).toInt()

        val tickerJob = launch {
            snapshotFlow { rotation.value }.collect { current ->
                val currentSlice = floor(current / sweepPerSlice).toInt()
                if (currentSlice != lastTickedSlice) {
                    val ticksToPlay = abs(currentSlice - lastTickedSlice).coerceAtMost(2)
                    repeat(ticksToPlay) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TICK_DURATION_MS)
                    }
                    lastTickedSlice = currentSlice
                }
            }
        }

        rotation.animateTo(
            targetValue = rotation.value + currentRequest.deltaRotationDegrees,
            animationSpec = tween(
                durationMillis = 4200,
                easing = FastOutSlowInEasing
            )
        )
        tickerJob.cancel()
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, FINAL_TONE_DURATION_MS)
        onSpinAnimationCompleted(currentRequest.requestId)
    }

    val isSpinning = pendingSpin != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RouletteWheel(
                names = names,
                rotationDegrees = rotation.value,
                isSpinning = isSpinning,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            SpinButton(
                enabled = names.isNotEmpty() && !isSpinning,
                isSpinning = isSpinning,
                onClick = { onSpinRequested(rotation.value) }
            )
        }
    }
}

@Composable
private fun SpinButton(
    enabled: Boolean,
    isSpinning: Boolean,
    onClick: () -> Unit
) {
    val gradient = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    id = if (isSpinning) {
                        R.string.roulette_action_spinning
                    } else {
                        R.string.roulette_action_spin
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RouletteWheel(
    names: List<String>,
    rotationDegrees: Float,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val emptyNamesText = stringResource(id = R.string.roulette_names_empty)

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    val textPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT_BOLD,
                android.graphics.Typeface.BOLD
            )
        }
    }

    val palette = remember {
        listOf(
            Color(0xFFEF4444) to Color(0xFFB91C1C), // red
            Color(0xFFF59E0B) to Color(0xFFD97706), // amber
            Color(0xFF22C55E) to Color(0xFF15803D), // green
            Color(0xFF06B6D4) to Color(0xFF0E7490), // cyan
            Color(0xFF6366F1) to Color(0xFF4338CA), // indigo
            Color(0xFFEC4899) to Color(0xFFBE185D), // pink
            Color(0xFF8B5CF6) to Color(0xFF6D28D9), // violet
            Color(0xFFF97316) to Color(0xFFC2410C)  // orange
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryContainerColor.copy(alpha = 0.45f),
                        surfaceVariantColor.copy(alpha = 0.25f),
                        surfaceColor
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.40f
            val wheelSize = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.25f
                ),
                radius = radius * 1.25f,
                center = center
            )

            // Outer gradient rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        tertiaryColor,
                        primaryColor
                    ),
                    center = center
                ),
                radius = radius + 6.dp.toPx(),
                center = center
            )
            // Rim inner shadow ring
            drawCircle(
                color = surfaceColor,
                radius = radius + 2.dp.toPx(),
                center = center
            )

            if (names.isEmpty()) {
                drawCircle(
                    color = surfaceVariantColor.copy(alpha = 0.7f),
                    radius = radius,
                    center = center
                )
                drawIntoCanvas { canvas ->
                    textPaint.color = onSurfaceVariantColor.toArgb()
                    textPaint.textSize = with(density) { 14.sp.toPx() }
                    val lines = emptyNamesText.split(". ", limit = 2)
                    val centerY = center.y - ((lines.size - 1) * textPaint.textSize / 2f)
                    lines.forEachIndexed { index, line ->
                        canvas.nativeCanvas.drawText(
                            line.trim(),
                            center.x,
                            centerY + index * textPaint.textSize * 1.2f,
                            textPaint
                        )
                    }
                }
            } else {
                val sweep = 360f / names.size
                names.forEachIndexed { index, name ->
                    val startAngle = rotationDegrees - 90f + (index * sweep)
                    val (light, dark) = palette[index % palette.size]

                    // Slice with radial gradient (lighter near rim, darker at center)
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(dark, light),
                            center = center,
                            radius = radius
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = topLeft,
                        size = wheelSize
                    )

                    // Slice separator (thin white line at start of slice)
                    val sepAngle = Math.toRadians(startAngle.toDouble())
                    val sepEndX = center.x + (cos(sepAngle) * radius).toFloat()
                    val sepEndY = center.y + (sin(sepAngle) * radius).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = center,
                        end = Offset(sepEndX, sepEndY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Label
                    val labelAngle = Math.toRadians((startAngle + (sweep / 2f)).toDouble())
                    val labelRadius = radius * 0.62f
                    val labelX = center.x + (cos(labelAngle) * labelRadius).toFloat()
                    val labelY = center.y + (sin(labelAngle) * labelRadius).toFloat()

                    drawIntoCanvas { canvas ->
                        textPaint.color = Color.White.toArgb()
                        textPaint.textSize = with(density) { 13.sp.toPx() }
                        textPaint.setShadowLayer(
                            with(density) { 2.dp.toPx() },
                            0f,
                            with(density) { 1.dp.toPx() },
                            Color.Black.copy(alpha = 0.45f).toArgb()
                        )
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.rotate(
                            startAngle + (sweep / 2f) + 90f,
                            labelX,
                            labelY
                        )
                        canvas.nativeCanvas.drawText(
                            name.take(14),
                            labelX,
                            labelY,
                            textPaint
                        )
                        canvas.nativeCanvas.restore()
                        textPaint.clearShadowLayer()
                    }
                }

                // Crisp outer ring outline above slices
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Multi-layer hub
            drawCircle(
                color = surfaceColor,
                radius = radius * 0.26f,
                center = center
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, tertiaryColor),
                    start = Offset(center.x - radius * 0.18f, center.y - radius * 0.18f),
                    end = Offset(center.x + radius * 0.18f, center.y + radius * 0.18f)
                ),
                radius = radius * 0.18f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = radius * 0.06f,
                center = center
            )

            // Modern pointer (teardrop style at top)
            val pointerTipY = center.y - radius - 10.dp.toPx()
            val pointerBaseY = center.y - radius + 8.dp.toPx()
            val pointerHalfWidth = 14.dp.toPx()
            val pointerPath = Path().apply {
                moveTo(center.x, pointerTipY)
                lineTo(center.x - pointerHalfWidth, pointerBaseY)
                lineTo(center.x + pointerHalfWidth, pointerBaseY)
                close()
            }
            // Pointer shadow halo
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = pointerHalfWidth + 6.dp.toPx(),
                center = Offset(center.x, pointerBaseY)
            )
            drawPath(
                path = pointerPath,
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, tertiaryColor),
                    start = Offset(center.x, pointerTipY),
                    end = Offset(center.x, pointerBaseY)
                )
            )
            // Pointer outline
            drawPath(
                path = pointerPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Pointer hub dot
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(center.x, pointerBaseY - 2.dp.toPx())
            )
            drawCircle(
                color = primaryColor,
                radius = 2.dp.toPx(),
                center = Offset(center.x, pointerBaseY - 2.dp.toPx())
            )
        }

        // Spinning hint overlay (subtle text below pointer)
        if (isSpinning && names.isNotEmpty()) {
            // No-op visual; the rotation itself gives feedback. Could add a pulse later.
        }
    }
}

private const val TONE_VOLUME = 70
private const val TICK_DURATION_MS = 30
private const val FINAL_TONE_DURATION_MS = 320

// created by Mories Deo Hutapea, S.E.,S.Kom
