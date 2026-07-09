package com.clove.browser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Tiny JSON-backed persistence over SharedPreferences. Keeps things dependency-free
 * (no Room / Gson) while still surviving process death for history, bookmarks,
 * reading list, downloads and settings.
 */
class BrowserStore(context: Context) {

    private val prefs = context.getSharedPreferences("clove_browser", Context.MODE_PRIVATE)

    // ---------------- Settings ----------------

    fun loadSettings(): Settings {
        val json = prefs.getString(KEY_SETTINGS, null) ?: return Settings()
        return try {
            val o = JSONObject(json)
            Settings(
                searchEngine = runCatching { SearchEngine.valueOf(o.optString("engine", "GOOGLE")) }
                    .getOrDefault(SearchEngine.GOOGLE),
                toolbarPosition = runCatching { ToolbarPosition.valueOf(o.optString("toolbar", "BOTTOM")) }
                    .getOrDefault(ToolbarPosition.BOTTOM),
                showSuggestions = o.optBoolean("suggestions", true),
                blockPopups = o.optBoolean("popups", true),
                doNotTrack = o.optBoolean("dnt", false),
            )
        } catch (e: Exception) {
            Settings()
        }
    }

    fun saveSettings(s: Settings) {
        val o = JSONObject()
            .put("engine", s.searchEngine.name)
            .put("toolbar", s.toolbarPosition.name)
            .put("suggestions", s.showSuggestions)
            .put("popups", s.blockPopups)
            .put("dnt", s.doNotTrack)
        prefs.edit().putString(KEY_SETTINGS, o.toString()).apply()
    }

    // ---------------- History ----------------

    fun loadHistory(): List<HistoryEntry> = readArray(KEY_HISTORY) { o ->
        HistoryEntry(o.getString("url"), o.optString("title"), o.optLong("ts"))
    }

    fun saveHistory(items: List<HistoryEntry>) = writeArray(KEY_HISTORY, items) { e ->
        JSONObject().put("url", e.url).put("title", e.title).put("ts", e.timestamp)
    }

    // ---------------- Bookmarks & reading list ----------------

    fun loadBookmarks(): List<Bookmark> = readArray(KEY_BOOKMARKS, ::bookmarkFromJson)
    fun saveBookmarks(items: List<Bookmark>) = writeArray(KEY_BOOKMARKS, items, ::bookmarkToJson)

    fun loadReadingList(): List<Bookmark> = readArray(KEY_READING, ::bookmarkFromJson)
    fun saveReadingList(items: List<Bookmark>) = writeArray(KEY_READING, items, ::bookmarkToJson)

    // ---------------- Downloads ----------------

    fun loadDownloads(): List<DownloadItem> = readArray(KEY_DOWNLOADS) { o ->
        DownloadItem(o.optLong("id"), o.getString("name"), o.optString("url"), o.optLong("ts"))
    }

    fun saveDownloads(items: List<DownloadItem>) = writeArray(KEY_DOWNLOADS, items) { d ->
        JSONObject().put("id", d.id).put("name", d.fileName).put("url", d.url).put("ts", d.startedAt)
    }

    // ---------------- helpers ----------------

    private fun bookmarkFromJson(o: JSONObject) =
        Bookmark(o.getString("url"), o.optString("title"), o.optLong("ts"))

    private fun bookmarkToJson(b: Bookmark) =
        JSONObject().put("url", b.url).put("title", b.title).put("ts", b.addedAt)

    private fun <T> readArray(key: String, map: (JSONObject) -> T): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                runCatching { map(arr.getJSONObject(i)) }.getOrNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun <T> writeArray(key: String, items: List<T>, map: (T) -> JSONObject) {
        val arr = JSONArray()
        items.forEach { arr.put(map(it)) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    companion object {
        private const val KEY_SETTINGS = "settings"
        private const val KEY_HISTORY = "history"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_READING = "reading_list"
        private const val KEY_DOWNLOADS = "downloads"
    }
}
