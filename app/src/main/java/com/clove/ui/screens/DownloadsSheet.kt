package com.clove.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clove.browser.DownloadItem
import com.clove.browser.prettyHost
import com.clove.ui.components.clickableCompat
import com.clove.ui.components.consumeClicks
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide

@Composable
fun DownloadsSheet(
    downloads: List<DownloadItem>,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBackground)
            .consumeClicks(),
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Downloads", color = palette.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (downloads.isNotEmpty()) {
                    Text(
                        "Clear", color = palette.accent, fontSize = 16.sp,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).then(clickableCompat(onClick = onClear)).padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Text(
                    "Done", color = palette.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).then(clickableCompat(onClick = onDismiss)).padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            if (downloads.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Lucide.Download, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(46.dp))
                    Text("No downloads", color = palette.textSecondary, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
                    Text(
                        "Files you download appear here and in your device's Downloads folder.",
                        color = palette.textSecondary, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 40.dp, end = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(downloads) { d ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).background(palette.glassElevated),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Lucide.Download, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(d.fileName, color = palette.textPrimary, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(prettyHost(d.url), color = palette.textSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
