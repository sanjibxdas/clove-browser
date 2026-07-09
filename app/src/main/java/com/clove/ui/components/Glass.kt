package com.clove.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.clove.ui.theme.LocalSafariPalette
import kotlin.math.roundToInt

/**
 * A frosted "liquid glass" surface. When a [LocalGlassBackdrop] is available (a
 * downscaled window snapshot), we sample the region directly behind this surface and
 * upscale it — a real backdrop blur that works even over a WebView. A translucent tint,
 * top-edge highlight and hairline border complete the iOS look; without a backdrop we
 * fall back to a solid frosted fill.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    val palette = LocalSafariPalette.current
    val backdropState = LocalGlassBackdrop.current
    val highlight = if (palette.isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.55f)
    // Lighter tint so the blurred page shows through; falls back gracefully before the
    // first capture (still a translucent frost over the page).
    val tint = if (palette.isDark) Color(0xFF1C1C22).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.5f)

    // NOTE: posInRoot is a plain holder read only in the draw phase — writing it does not
    // recompose, and neither does a new backdrop capture (read via backdropState.value below).
    val posInRoot = remember { floatArrayOf(0f, 0f) }

    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false, ambientColor = Color.Black.copy(0.25f), spotColor = Color.Black.copy(0.30f))
            .onGloballyPositioned {
                val p = it.positionInRoot()
                posInRoot[0] = p.x
                posInRoot[1] = p.y
            }
            .clip(shape)
            .drawBehind {
                val backdrop = backdropState?.value
                if (backdrop != null) drawBackdropCrop(backdrop, Offset(posInRoot[0], posInRoot[1]), size)
            }
            .background(tint)
            .background(
                Brush.verticalGradient(
                    0f to highlight,
                    0.5f to Color.Transparent,
                )
            )
            .border(BorderStroke(0.8.dp, palette.glassBorder), shape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.textPrimary) {
            content()
        }
    }
}

/** Draws the slice of the window snapshot sitting behind this surface, upscaled (soft). */
private fun DrawScope.drawBackdropCrop(backdrop: GlassBackdrop, posInRoot: Offset, size: Size) {
    val img = backdrop.image
    if (backdrop.rootWidth <= 0 || backdrop.rootHeight <= 0) return
    val sx = img.width.toFloat() / backdrop.rootWidth
    val sy = img.height.toFloat() / backdrop.rootHeight

    val srcLeft = (posInRoot.x * sx).roundToInt().coerceIn(0, (img.width - 1).coerceAtLeast(0))
    val srcTop = (posInRoot.y * sy).roundToInt().coerceIn(0, (img.height - 1).coerceAtLeast(0))
    val srcW = (size.width * sx).roundToInt().coerceIn(1, img.width - srcLeft)
    val srcH = (size.height * sy).roundToInt().coerceIn(1, img.height - srcTop)

    drawImage(
        image = img,
        srcOffset = IntOffset(srcLeft, srcTop),
        srcSize = IntSize(srcW, srcH),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1)),
        filterQuality = FilterQuality.Low, // bilinear; the heavy downscale already softens it
    )
}

/** A circular glass button used at the ends of the toolbar (back / more). */
@Composable
fun GlassCircleButton(
    onClick: () -> Unit,
    size: Dp = 46.dp,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .then(
                clickableCompat(onClick, onLongClick, enabled, interaction)
            ),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            elevation = 10.dp,
        ) {
            Box(
                modifier = Modifier.graphicsLayer { alpha = if (enabled) 1f else 0.35f },
                contentAlignment = Alignment.Center,
            ) { content() }
        }
    }
}
