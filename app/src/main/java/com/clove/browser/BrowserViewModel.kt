package com.clove.browser

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import java.net.URLEncoder

class BrowserViewModel(app: Application) : AndroidViewModel(app) {

    private val store = BrowserStore(app)

    // ---- persistent collections ----
    val history: SnapshotStateList<HistoryEntry> = mutableStateListOf()
    val bookmarks: SnapshotStateList<Bookmark> = mutableStateListOf()
    val readingList: SnapshotStateList<Bookmark> = mutableStateListOf()
    val downloads: SnapshotStateList<DownloadItem> = mutableStateListOf()

    var settings by mutableStateOf(Settings())
        private set

    // ---- tabs ----
    val tabs: SnapshotStateList<TabState> = mutableStateListOf()
    var currentTabId by mutableStateOf<String?>(null)
        private set
    var isPrivateMode by mutableStateOf(false)
        private set

    init {
        settings = store.loadSettings()
        history.addAll(store.loadHistory())
        bookmarks.addAll(store.loadBookmarks())
        readingList.addAll(store.loadReadingList())
        downloads.addAll(store.loadDownloads())
        // Start with a single, empty normal tab (native Start Page).
        val first = TabState(isPrivate = false)
        tabs.add(first)
        currentTabId = first.id
    }

    // ---------------- derived ----------------

    val currentTab: TabState?
        get() = tabs.firstOrNull { it.id == currentTabId }

    /** Tabs belonging to the currently active environment (normal vs private). */
    val visibleTabs: List<TabState>
        get() = tabs.filter { it.isPrivate == isPrivateMode }

    val visibleTabCount: Int get() = visibleTabs.size

    // ---------------- navigation intent ----------------

    /** Interpret the address-bar text as either a URL or a search query and load it. */
    fun submitQuery(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        val url = if (isProbablyUrl(input)) normalizeUrl(input) else searchUrl(input)
        val tab = currentTab ?: newTab(isPrivateMode)
        loadInTab(tab, url)
    }

    fun loadInTab(tab: TabState, url: String) {
        tab.pendingUrl = url
        tab.url = url
        tab.isLoading = true
    }

    private fun searchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return settings.searchEngine.queryTemplate.replace("%s", encoded)
    }

    private fun normalizeUrl(input: String): String = when {
        input.startsWith("http://") || input.startsWith("https://") ||
            input.startsWith("about:") || input.startsWith("file:") -> input
        else -> "https://$input"
    }

    private fun isProbablyUrl(s: String): Boolean {
        if (s.contains(" ")) return false
        if (s.startsWith("http://") || s.startsWith("https://") ||
            s.startsWith("about:") || s.startsWith("file:")
        ) return true
        if (s == "localhost" || s.startsWith("localhost")) return true
        // e.g. "apple.com", "sub.domain.co.uk/path"
        return Regex("^[\\w-]+(\\.[\\w-]+)+(/.*)?$").matches(s)
    }

    // ---------------- tab management ----------------

    fun newTab(private: Boolean = isPrivateMode, url: String? = null, select: Boolean = true): TabState {
        val tab = TabState(isPrivate = private, initialUrl = url)
        tabs.add(tab)
        if (select) {
            isPrivateMode = private
            currentTabId = tab.id
        }
        return tab
    }

    fun selectTab(id: String) {
        val tab = tabs.firstOrNull { it.id == id } ?: return
        isPrivateMode = tab.isPrivate
        currentTabId = id
    }

    fun closeTab(id: String) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return
        val closing = tabs[index]
        tabs.removeAt(index)
        if (currentTabId == id) {
            // Select a neighbouring tab within the same environment, else create a fresh one.
            val sameMode = tabs.filter { it.isPrivate == closing.isPrivate }
            currentTabId = sameMode.lastOrNull()?.id ?: run {
                val fresh = TabState(isPrivate = closing.isPrivate)
                tabs.add(fresh)
                fresh.id
            }
        }
    }

    fun closeAllVisibleTabs() {
        val toClose = tabs.filter { it.isPrivate == isPrivateMode }.map { it.id }
        toClose.forEach { id -> tabs.removeAll { it.id == id } }
        val fresh = TabState(isPrivate = isPrivateMode)
        tabs.add(fresh)
        currentTabId = fresh.id
    }

    fun switchPrivateMode(private: Boolean) {
        if (private == isPrivateMode) return
        isPrivateMode = private
        val existing = tabs.firstOrNull { it.isPrivate == private }
        currentTabId = existing?.id ?: newTab(private, select = false).also { it }.id
    }

    // ---------------- history ----------------

    fun recordVisit(url: String, title: String) {
        if (isPrivateMode || url.isBlank() || url.startsWith("about:")) return
        // De-dupe consecutive identical urls.
        if (history.firstOrNull()?.url == url) return
        history.add(0, HistoryEntry(url, title, System.currentTimeMillis()))
        while (history.size > 500) history.removeAt(history.size - 1)
        store.saveHistory(history)
    }

    fun clearHistory() {
        history.clear()
        store.saveHistory(history)
    }

    fun removeHistory(entry: HistoryEntry) {
        history.remove(entry)
        store.saveHistory(history)
    }

    // ---------------- bookmarks / reading list ----------------

    fun isBookmarked(url: String): Boolean = bookmarks.any { it.url == url }

    fun toggleBookmark(url: String, title: String) {
        if (url.isBlank()) return
        val existing = bookmarks.firstOrNull { it.url == url }
        if (existing != null) bookmarks.remove(existing)
        else bookmarks.add(0, Bookmark(url, title))
        store.saveBookmarks(bookmarks)
    }

    fun removeBookmark(bookmark: Bookmark) {
        bookmarks.remove(bookmark)
        store.saveBookmarks(bookmarks)
    }

    fun addToReadingList(url: String, title: String) {
        if (url.isBlank() || readingList.any { it.url == url }) return
        readingList.add(0, Bookmark(url, title))
        store.saveReadingList(readingList)
    }

    fun removeFromReadingList(bookmark: Bookmark) {
        readingList.remove(bookmark)
        store.saveReadingList(readingList)
    }

    // ---------------- downloads ----------------

    fun enqueueDownload(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        val app = getApplication<Application>()
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                userAgent?.let { addRequestHeader("User-Agent", it) }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setTitle(fileName)
                setDescription("Downloading via Clove")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            val dm = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = dm.enqueue(request)
            downloads.add(0, DownloadItem(id, fileName, url))
            store.saveDownloads(downloads)
            Toast.makeText(app, "Downloading $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(app, "Couldn't start download", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearDownloads() {
        downloads.clear()
        store.saveDownloads(downloads)
    }

    // ---------------- settings ----------------

    fun updateSettings(transform: (Settings) -> Settings) {
        settings = transform(settings)
        store.saveSettings(settings)
    }
}
