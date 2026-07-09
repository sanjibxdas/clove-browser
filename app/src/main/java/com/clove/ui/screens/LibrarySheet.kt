package com.clove.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clove.browser.Bookmark
import com.clove.browser.HistoryEntry
import com.clove.browser.prettyHost
import com.clove.ui.components.GlassSurface
import com.clove.ui.components.clickableCompat
import com.clove.ui.components.consumeClicks
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Glasses
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2

enum class LibraryTab { BOOKMARKS, READING, HISTORY }

@Composable
fun LibrarySheet(
    initialTab: LibraryTab,
    bookmarks: List<Bookmark>,
    readingList: List<Bookmark>,
    history: List<HistoryEntry>,
    onOpen: (String) -> Unit,
    onRemoveBookmark: (Bookmark) -> Unit,
    onRemoveReading: (Bookmark) -> Unit,
    onRemoveHistory: (HistoryEntry) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    var tab by remember { mutableStateOf(initialTab) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBackground)
            .consumeClicks(),
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Library",
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (tab == LibraryTab.HISTORY && history.isNotEmpty()) {
                    Text(
                        text = "Clear",
                        color = palette.accent,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .then(clickableCompat(onClick = onClearHistory))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = "Done",
                    color = palette.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(clickableCompat(onClick = onDismiss))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            // Segmented control
            SegmentedTabs(tab) { tab = it }

            when (tab) {
                LibraryTab.BOOKMARKS -> LinkList(
                    rows = bookmarks.map { LinkRow(it.title, it.url) },
                    emptyText = "No bookmarks yet",
                    emptyIcon = Lucide.BookOpen,
                    onOpen = onOpen,
                    onRemove = { row -> bookmarks.firstOrNull { it.url == row.url }?.let(onRemoveBookmark) },
                )
                LibraryTab.READING -> LinkList(
                    rows = readingList.map { LinkRow(it.title, it.url) },
                    emptyText = "Your Reading List is empty",
                    emptyIcon = Lucide.Glasses,
                    onOpen = onOpen,
                    onRemove = { row -> readingList.firstOrNull { it.url == row.url }?.let(onRemoveReading) },
                )
                LibraryTab.HISTORY -> LinkList(
                    rows = history.map { LinkRow(it.title, it.url, it) },
                    emptyText = "No history",
                    emptyIcon = Lucide.Clock,
                    onOpen = onOpen,
                    onRemove = { row -> (row.payload as? HistoryEntry)?.let(onRemoveHistory) },
                )
            }
        }
    }
}

private data class LinkRow(val title: String, val url: String, val payload: Any? = null)

@Composable
private fun SegmentedTabs(selected: LibraryTab, onSelect: (LibraryTab) -> Unit) {
    val palette = LocalSafariPalette.current
    GlassSurface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = 4.dp,
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            Segment(Lucide.BookOpen, "Bookmarks", selected == LibraryTab.BOOKMARKS) { onSelect(LibraryTab.BOOKMARKS) }
            Segment(Lucide.Glasses, "Reading", selected == LibraryTab.READING) { onSelect(LibraryTab.READING) }
            Segment(Lucide.Clock, "History", selected == LibraryTab.HISTORY) { onSelect(LibraryTab.HISTORY) }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Segment(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) palette.accent else androidx.compose.ui.graphics.Color.Transparent)
            .then(clickableCompat(onClick = onClick)),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) androidx.compose.ui.graphics.Color.White else palette.textSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "  $label",
                color = if (active) androidx.compose.ui.graphics.Color.White else palette.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LinkList(
    rows: List<LinkRow>,
    emptyText: String,
    emptyIcon: ImageVector,
    onOpen: (String) -> Unit,
    onRemove: (LinkRow) -> Unit,
) {
    val palette = LocalSafariPalette.current
    if (rows.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(emptyIcon, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(46.dp))
            Text(emptyText, color = palette.textSecondary, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
    ) {
        items(rows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(clickableCompat(onClick = { onOpen(row.url) }))
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.glassElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Globe, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = row.title.ifBlank { prettyHost(row.url) },
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = prettyHost(row.url),
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .then(clickableCompat(onClick = { onRemove(row) })),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Trash2, contentDescription = "Remove", tint = palette.textSecondary, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
