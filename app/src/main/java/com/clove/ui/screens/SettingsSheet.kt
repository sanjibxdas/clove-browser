package com.clove.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clove.browser.SearchEngine
import com.clove.browser.Settings
import com.clove.browser.ToolbarPosition
import com.clove.ui.components.GlassSurface
import com.clove.ui.components.clickableCompat
import com.clove.ui.components.consumeClicks
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide

@Composable
fun SettingsSheet(
    settings: Settings,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBackground)
            .consumeClicks(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Settings", color = palette.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "Done", color = palette.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).then(clickableCompat(onClick = onDismiss)).padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            SectionLabel("Search Engine")
            SettingsGroup {
                SearchEngine.values().forEachIndexed { i, engine ->
                    ChoiceRow(
                        label = engine.label,
                        selected = settings.searchEngine == engine,
                        showDivider = i < SearchEngine.values().lastIndex,
                        onClick = { onUpdate { it.copy(searchEngine = engine) } },
                    )
                }
            }

            SectionLabel("Address Bar")
            SettingsGroup {
                ChoiceRow("Bottom (Compact)", settings.toolbarPosition == ToolbarPosition.BOTTOM, true) {
                    onUpdate { it.copy(toolbarPosition = ToolbarPosition.BOTTOM) }
                }
                ChoiceRow("Top", settings.toolbarPosition == ToolbarPosition.TOP, false) {
                    onUpdate { it.copy(toolbarPosition = ToolbarPosition.TOP) }
                }
            }

            SectionLabel("General")
            SettingsGroup {
                ToggleRow("Show Search Suggestions", settings.showSuggestions, true) { v -> onUpdate { it.copy(showSuggestions = v) } }
                ToggleRow("Block Pop-ups", settings.blockPopups, true) { v -> onUpdate { it.copy(blockPopups = v) } }
                ToggleRow("Ask Websites Not to Track Me", settings.doNotTrack, false) { v -> onUpdate { it.copy(doNotTrack = v) } }
            }

            Box(modifier = Modifier.size(80.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val palette = LocalSafariPalette.current
    Text(
        text = text.uppercase(),
        color = palette.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 30.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    GlassSurface(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 3.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, showDivider: Boolean, onClick: () -> Unit) {
    val palette = LocalSafariPalette.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickableCompat(onClick = onClick))
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = palette.textPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
            if (selected) {
                Icon(Lucide.Check, contentDescription = "Selected", tint = palette.accent, modifier = Modifier.size(20.dp))
            }
        }
        if (showDivider) Divider()
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, showDivider: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalSafariPalette.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = palette.textPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedTrackColor = palette.accent),
            )
        }
        if (showDivider) Divider()
    }
}

@Composable
private fun Divider() {
    val palette = LocalSafariPalette.current
    Box(
        modifier = Modifier
            .padding(start = 18.dp)
            .fillMaxWidth()
            .height(0.6.dp)
            .background(palette.separator)
    )
}
