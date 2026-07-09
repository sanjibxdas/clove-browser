package com.clove.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.clove.browser.TabState
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Glasses
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.SquarePlus

@Composable
fun MoreMenu(
    tab: TabState?,
    isBookmarked: Boolean,
    onShare: () -> Unit,
    onBookmarks: () -> Unit,
    onAllTabs: () -> Unit,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onAddReadingList: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloads: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    val hasPage = tab?.url?.isNotBlank() == true

    Popup(
        alignment = Alignment.BottomEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        // Scrim behind the card so a tap anywhere dismisses.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(clickableCompat(onClick = onDismiss)),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Box(
                modifier = Modifier.padding(end = 10.dp, bottom = 78.dp),
            ) {
                GlassSurface(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(26.dp),
                    elevation = 26.dp,
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // Quick-action trio
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            QuickAction(Lucide.Share2, "Share", enabled = hasPage) { onShare() }
                            QuickAction(Lucide.BookOpen, "Bookmarks") { onBookmarks() }
                            QuickAction(Lucide.Layers, "All Tabs") { onAllTabs() }
                        }
                        MenuDivider()
                        MenuRow(Lucide.SquarePlus, "New Tab") { onNewTab() }
                        MenuRow(Lucide.EyeOff, "New Private Tab") { onNewPrivateTab() }
                        MenuDivider()
                        MenuRow(Lucide.Glasses, "Add to Reading List", enabled = hasPage) { onAddReadingList() }
                        MenuRow(
                            Lucide.Bookmark,
                            if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                            enabled = hasPage,
                            tint = if (isBookmarked) palette.accent else palette.textPrimary,
                        ) { onToggleBookmark() }
                        MenuDivider()
                        MenuRow(Lucide.Download, "Downloads") { onDownloads() }
                        MenuRow(Lucide.Clock, "History") { onHistory() }
                        MenuRow(Lucide.Settings, "Settings") { onSettings() }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    val alpha = if (enabled) 1f else 0.4f
    Column(
        modifier = Modifier
            .width(76.dp)
            .then(clickableCompat(onClick = onClick, enabled = enabled)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = palette.textPrimary.copy(alpha = alpha),
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            color = palette.textPrimary.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    val content = (tint ?: palette.textPrimary).copy(alpha = if (enabled) 1f else 0.4f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableCompat(onClick = onClick, enabled = enabled))
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun MenuDivider() {
    val palette = LocalSafariPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(0.6.dp)
            .background(palette.separator)
    )
}
