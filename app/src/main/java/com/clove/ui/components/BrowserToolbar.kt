package com.clove.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.clove.browser.TabState
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Share
import com.composables.icons.lucide.X

/**
 * The floating iOS 26 "Bottom" toolbar — two tiers:
 *
 *   [ 🔒   domain                       ⟳ ]   <- address pill (lock left, reload right)
 *   ( ‹ ) ( › ) ( ⬆ ) ( ▤ ) ( ⧉ )             <- glass button row (long-press ⧉ = More)
 *
 * As you scroll down ([collapseFraction] 0f→1f) the button row fades & collapses and the
 * pill narrows to a minimized centered capsule; scrolling up expands it again.
 */
@Composable
fun BrowserToolbar(
    tab: TabState?,
    collapseFraction: Float,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onShare: () -> Unit,
    onReloadOrStop: () -> Unit,
    onAddressTap: () -> Unit,
    onMore: () -> Unit,
    onBookmarks: () -> Unit,
    onTabs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalSafariPalette.current
    val f = collapseFraction.coerceIn(0f, 1f)

    val pillHeight: Dp = lerp(50.dp, 44.dp, f)
    val edgeIconAlpha = 1f - f
    val pillInset: Dp = lerp(0.dp, 52.dp, f)
    val rowHeight: Dp = lerp(46.dp, 0.dp, f)
    val rowAlpha = (1f - f * 1.7f).coerceIn(0f, 1f)
    val gap: Dp = lerp(9.dp, 0.dp, f)

    val canGoBack = tab?.canGoBack == true
    val canGoForward = tab?.canGoForward == true
    val isLoading = tab?.isLoading == true
    val host = tab?.host ?: ""
    val isSecure = tab?.url?.startsWith("https://") == true

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ---------- Address pill ----------
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = pillInset)
                .height(pillHeight),
            shape = RoundedCornerShape(pillHeight / 2),
            elevation = 14.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (host.isBlank()) {
                    // Start page: centered "Search or enter website" hint.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(clickableCompat(onClick = onAddressTap)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Lucide.Search, null, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                            Text(
                                "  Search or enter website",
                                color = palette.textSecondary, fontSize = 16.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    // Lock icon, hard left.
                    Icon(
                        if (isSecure) Lucide.Lock else Lucide.Globe,
                        contentDescription = if (isSecure) "Secure" else "Site",
                        tint = palette.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer { alpha = edgeIconAlpha },
                    )
                    // Domain, centered in between.
                    Text(
                        text = host,
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .then(clickableCompat(onClick = onAddressTap)),
                    )
                    // Reload / stop, hard right.
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = edgeIconAlpha }
                            .size(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(clickableCompat(onClick = onReloadOrStop)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (isLoading) Lucide.X else Lucide.RotateCw,
                            contentDescription = if (isLoading) "Stop" else "Reload",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // ---------- Toolbar button row (collapses on scroll) ----------
        if (rowHeight > 0.5.dp) {
            Box(modifier = Modifier.height(gap))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .clipToBounds()
                    .padding(horizontal = 4.dp)
                    .graphicsLayer { alpha = rowAlpha },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BarButton(Lucide.ArrowLeft, "Back", canGoBack, onBack)
                BarButton(Lucide.ArrowRight, "Forward", canGoForward, onForward)
                BarButton(Lucide.Share, "Share", host.isNotBlank(), onShare)
                BarButton(Lucide.BookOpen, "Bookmarks", true, onBookmarks)
                BarButton(Lucide.Copy, "Tabs", true, onTabs, onLongClick = onMore)
            }
        }
    }
}

/** A glass-contained toolbar icon button (bottom row). Long-press supported for overflow. */
@Composable
private fun BarButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val palette = LocalSafariPalette.current
    GlassCircleButton(onClick = onClick, size = 46.dp, enabled = enabled, onLongClick = onLongClick) {
        Icon(icon, contentDescription = label, tint = palette.textPrimary, modifier = Modifier.size(22.dp))
    }
}
