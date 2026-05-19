package com.lampung.baktimarsada.feature.roulette.presentation

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.model.RoulettePendingSpin
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun RouletteWheelCard(
    names: List<String>,
    pendingSpin: RoulettePendingSpin?,
    onSpinRequested: (Float) -> Boolean,
    onSpinAnimationCompleted: (Long) -> Unit
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(pendingSpin?.requestId) {
        val currentRequest = pendingSpin ?: return@LaunchedEffect
        rotation.animateTo(
            targetValue = rotation.value + currentRequest.deltaRotationDegrees,
            animationSpec = tween(
                durationMillis = 4200,
                easing = FastOutSlowInEasing
            )
        )
        onSpinAnimationCompleted(currentRequest.requestId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Button(
                onClick = { onSpinRequested(rotation.value) },
                enabled = names.isNotEmpty() && pendingSpin == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.roulette_action_spin))
            }
        }
    }
}

@Composable
private fun RouletteWheel(
    names: List<String>,
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val emptyNamesText = stringResource(id = R.string.roulette_names_empty)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val textPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
    }
    val palette = listOf(
        Color(0xFFF97316),
        Color(0xFFEA580C),
        Color(0xFF0F766E),
        Color(0xFF2563EB),
        Color(0xFFBE123C),
        Color(0xFF7C3AED)
    )
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.38f
                val wheelSize = Size(radius * 2f, radius * 2f)
                val topLeft = Offset(center.x - radius, center.y - radius)

                val pointerPath = Path().apply {
                    moveTo(center.x, center.y - radius - 18.dp.toPx())
                    lineTo(center.x - 12.dp.toPx(), center.y - radius + 6.dp.toPx())
                    lineTo(center.x + 12.dp.toPx(), center.y - radius + 6.dp.toPx())
                    close()
                }
                drawPath(
                    path = pointerPath,
                    color = tertiaryColor
                )

                if (names.isEmpty()) {
                    drawCircle(
                        color = surfaceVariantColor,
                        radius = radius,
                        center = center
                    )
                    drawIntoCanvas { canvas ->
                        textPaint.color = onSurfaceVariantColor.toArgb()
                        textPaint.textSize = with(density) { 16.sp.toPx() }
                        canvas.nativeCanvas.drawText(
                            emptyNamesText,
                            center.x,
                            center.y,
                            textPaint
                        )
                    }
                } else {
                    val sweep = 360f / names.size
                    names.forEachIndexed { index, name ->
                        val startAngle = rotationDegrees - 90f + (index * sweep)
                        drawArc(
                            color = palette[index % palette.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = wheelSize
                        )

                        val labelAngle = Math.toRadians((startAngle + (sweep / 2f)).toDouble())
                        val labelRadius = radius * 0.62f
                        val labelX = center.x + (cos(labelAngle) * labelRadius).toFloat()
                        val labelY = center.y + (sin(labelAngle) * labelRadius).toFloat()

                        drawIntoCanvas { canvas ->
                            textPaint.color = Color.White.toArgb()
                            textPaint.textSize = with(density) { 12.sp.toPx() }
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
                        }
                    }
                }

                drawCircle(
                    color = surfaceColor,
                    radius = radius * 0.22f,
                    center = center
                )
                drawCircle(
                    color = primaryColor,
                    radius = radius * 0.1f,
                    center = center
                )
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
