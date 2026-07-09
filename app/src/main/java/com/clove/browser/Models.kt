package com.clove.browser

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

/** A single browser tab. Its observable fields drive the chrome (address bar, progress, etc.). */
class TabState(
    val id: String = UUID.randomUUID().toString(),
    val isPrivate: Boolean = false,
    initialUrl: String? = null,
) {
    /** The committed page url (empty => show native Start Page). */
    var url by mutableStateOf(initialUrl ?: "")
    var title by mutableStateOf("")
    var progress by mutableStateOf(0f)
    var isLoading by mutableStateOf(false)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)

    /** Set when we want the underlying WebView to (re)load a url on next composition. */
    var pendingUrl by mutableStateOf(initialUrl)

    /** Snapshot used inside the tab switcher grid. */
    var thumbnail by mutableStateOf<Bitmap?>(null)
    var favicon by mutableStateOf<Bitmap?>(null)

    val hasContent: Boolean get() = url.isNotBlank() || pendingUrl != null

    /** Best-effort display host, e.g. "apple.com". */
    val host: String
        get() = prettyHost(url)

    val displayTitle: String
        get() = when {
            title.isNotBlank() -> title
            host.isNotBlank() -> host
            else -> "Start Page"
        }
}

data class HistoryEntry(
    val url: String,
    val title: String,
    val timestamp: Long,
)

data class Bookmark(
    val url: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis(),
)

data class DownloadItem(
    val id: Long,
    val fileName: String,
    val url: String,
    val startedAt: Long = System.currentTimeMillis(),
)

enum class SearchEngine(val label: String, val queryTemplate: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s", "https://www.google.com"),
    BING("Bing", "https://www.bing.com/search?q=%s", "https://www.bing.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s", "https://duckduckgo.com"),
    YAHOO("Yahoo", "https://search.yahoo.com/search?p=%s", "https://www.yahoo.com");
}

/** Where the floating address bar lives. iOS 26 offers Bottom (default) and Top. */
enum class ToolbarPosition { BOTTOM, TOP }

data class Settings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val toolbarPosition: ToolbarPosition = ToolbarPosition.BOTTOM,
    val showSuggestions: Boolean = true,
    val blockPopups: Boolean = true,
    val doNotTrack: Boolean = false,
)

/** Turns a raw url into a compact host label, dropping "www." and scheme. */
fun prettyHost(raw: String): String {
    if (raw.isBlank()) return ""
    return try {
        val uri = android.net.Uri.parse(if (raw.contains("://")) raw else "https://$raw")
        (uri.host ?: raw).removePrefix("www.")
    } catch (e: Exception) {
        raw
    }
}
