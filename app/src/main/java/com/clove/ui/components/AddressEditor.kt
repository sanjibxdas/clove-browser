package com.clove.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clove.browser.Bookmark
import com.clove.browser.HistoryEntry
import com.clove.browser.prettyHost
import com.clove.ui.theme.LocalSafariPalette
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Star

private data class Suggestion(val title: String, val url: String, val kind: Kind) {
    enum class Kind { BOOKMARK, HISTORY, SEARCH }
}

@Composable
fun AddressEditor(
    initialUrl: String,
    searchEngineLabel: String,
    history: List<HistoryEntry>,
    bookmarks: List<Bookmark>,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalSafariPalette.current
    val focusRequester = remember { FocusRequester() }

    var field by remember {
        mutableStateOf(TextFieldValue(initialUrl, TextRange(0, initialUrl.length)))
    }
    val query = field.text.trim()

    val suggestions = remember(query, history, bookmarks) {
        buildSuggestions(query, history, bookmarks, searchEngineLabel)
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.scrim)
            .then(clickableCompat(onClick = onDismiss)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // Suggestions list (grows upward, nearest the field)
            if (suggestions.isNotEmpty()) {
                GlassSurface(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 16.dp,
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        items(suggestions) { s ->
                            SuggestionRow(s) { onSubmit(if (s.kind == Suggestion.Kind.SEARCH) s.title else s.url) }
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
            }

            // The editable field + Cancel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 14.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Lucide.Search,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                        BasicTextField(
                            value = field,
                            onValueChange = { field = it },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .focusRequester(focusRequester),
                            textStyle = LocalTextStyle.current.copy(
                                color = palette.textPrimary,
                                fontSize = 17.sp,
                            ),
                            cursorBrush = SolidColor(palette.accent),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go,
                                autoCorrect = false,
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = { if (query.isNotEmpty()) onSubmit(query) }
                            ),
                            decorationBox = { inner ->
                                if (field.text.isEmpty()) {
                                    Text(
                                        "Search or enter website name",
                                        color = palette.textSecondary,
                                        fontSize = 17.sp,
                                    )
                                }
                                inner()
                            },
                        )
                    }
                }
                Text(
                    text = "Cancel",
                    color = palette.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(clickableCompat(onClick = onDismiss))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(s: Suggestion, onClick: () -> Unit) {
    val palette = LocalSafariPalette.current
    val icon = when (s.kind) {
        Suggestion.Kind.BOOKMARK -> Lucide.Star
        Suggestion.Kind.HISTORY -> Lucide.Clock
        Suggestion.Kind.SEARCH -> Lucide.Search
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableCompat(onClick = onClick))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (s.kind == Suggestion.Kind.SEARCH) icon else Lucide.Globe,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = s.title,
                color = palette.textPrimary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (s.kind != Suggestion.Kind.SEARCH && s.url.isNotBlank()) {
                Text(
                    text = prettyHost(s.url),
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun buildSuggestions(
    query: String,
    history: List<HistoryEntry>,
    bookmarks: List<Bookmark>,
    engineLabel: String,
): List<Suggestion> {
    val result = mutableListOf<Suggestion>()
    if (query.isNotEmpty()) {
        result.add(Suggestion("$query — Search $engineLabel", query, Suggestion.Kind.SEARCH))
    }
    val q = query.lowercase()
    bookmarks.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.url.lowercase().contains(q) }
        .take(4)
        .forEach { result.add(Suggestion(it.title.ifBlank { prettyHost(it.url) }, it.url, Suggestion.Kind.BOOKMARK)) }
    history.asSequence()
        .filter { q.isEmpty() || it.title.lowercase().contains(q) || it.url.lowercase().contains(q) }
        .distinctBy { it.url }
        .take(6)
        .forEach { result.add(Suggestion(it.title.ifBlank { prettyHost(it.url) }, it.url, Suggestion.Kind.HISTORY)) }
    return result.take(10)
}
