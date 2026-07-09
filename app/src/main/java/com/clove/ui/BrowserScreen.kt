package com.clove.ui

import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.clove.browser.BrowserViewModel
import com.clove.browser.web.captureThumbnail
import com.clove.browser.web.createBrowserWebView
import com.clove.ui.components.AddressEditor
import com.clove.ui.components.BrowserToolbar
import com.clove.ui.components.LocalGlassBackdrop
import com.clove.ui.components.MoreMenu
import com.clove.ui.components.rememberGlassBackdrop
import com.clove.ui.screens.DownloadsSheet
import com.clove.ui.screens.LibrarySheet
import com.clove.ui.screens.LibraryTab
import com.clove.ui.screens.SettingsSheet
import com.clove.ui.screens.StartPage
import com.clove.ui.screens.TabsOverview
import com.clove.ui.theme.LocalSafariPalette

private enum class Overlay { NONE, TABS, LIBRARY, DOWNLOADS, SETTINGS }

@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val palette = LocalSafariPalette.current
    val context = LocalContext.current

    // One live WebView per tab id, kept alive across tab switches.
    val webViews = remember { mutableStateMapOf<String, WebView>() }

    var overlay by remember { mutableStateOf(Overlay.NONE) }
    var libraryTab by remember { mutableStateOf(LibraryTab.BOOKMARKS) }
    var showMore by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf(false) }

    // ---- live frosted-glass backdrop (real blur over the WebView) ----
    val (glassBackdropState, captureBackdrop) = rememberGlassBackdrop()

    // ---- scroll driven collapse ----
    val collapsed = remember { mutableStateOf(false) }
    val onScroll = remember {
        { dy: Int, scrollY: Int ->
            collapsed.value = when {
                scrollY < 48 -> false
                dy > 6 -> true
                dy < -6 -> false
                else -> collapsed.value
            }
            captureBackdrop() // throttled internally; keeps the blur fresh while scrolling
        }
    }
    val collapseFraction by animateFloatAsState(
        targetValue = if (collapsed.value) 1f else 0f,
        label = "collapse",
    )

    val tab = viewModel.currentTab

    // Destroy WebViews whose tabs were closed.
    val tabIds = viewModel.tabs.map { it.id }
    androidx.compose.runtime.LaunchedEffect(tabIds) {
        val stale = webViews.keys.filter { it !in tabIds }
        stale.forEach { id -> webViews.remove(id)?.destroy() }
    }

    // Refresh the frosted backdrop shortly after loads / navigation / chrome reappearing.
    LaunchedEffect(tab?.id, tab?.isLoading, tab?.url, overlay, editingAddress) {
        if (overlay == Overlay.NONE && !editingAddress) {
            kotlinx.coroutines.delay(160)
            captureBackdrop()
        }
    }

    fun activeWebView(): WebView? = viewModel.currentTabId?.let { webViews[it] }

    fun captureCurrentThumbnail() {
        activeWebView()?.captureThumbnail()?.let { bmp -> tab?.thumbnail = bmp }
    }

    fun shareCurrent() {
        val url = tab?.url ?: return
        if (url.isBlank()) return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            putExtra(Intent.EXTRA_SUBJECT, tab.title)
        }
        context.startActivity(Intent.createChooser(send, "Share"))
    }

    // ---- system back ----
    val handleBack = editingAddress || showMore || overlay != Overlay.NONE || tab?.canGoBack == true
    BackHandler(enabled = handleBack) {
        when {
            editingAddress -> editingAddress = false
            showMore -> showMore = false
            overlay != Overlay.NONE -> overlay = Overlay.NONE
            tab?.canGoBack == true -> activeWebView()?.goBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBackground),
    ) {
        // ---------- Page content (inset below the status bar, like other browsers) ----------
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            if (tab != null && tab.hasContent) {
                key(tab.id) {
                    AndroidView(
                        factory = {
                            val wv = webViews.getOrPut(tab.id) {
                                createBrowserWebView(context, tab, viewModel, onScroll)
                            }
                            (wv.parent as? ViewGroup)?.removeView(wv)
                            wv
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { wv ->
                            val pending = tab.pendingUrl
                            if (pending != null) {
                                wv.loadUrl(pending)
                                tab.pendingUrl = null
                            }
                        },
                    )
                }
            } else {
                StartPage(
                    isPrivate = viewModel.isPrivateMode,
                    onOpen = { url -> tab?.let { viewModel.loadInTab(it, url) } },
                )
            }
        }

        // ---------- Top loading hairline ----------
        val loading = tab?.isLoading == true
        val progress = tab?.progress ?: 0f
        if (loading && progress > 0.02f && progress < 0.99f) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(2.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(2.5.dp)
                        .background(palette.accent),
                )
            }
        }

        // ---------- Floating toolbar ----------
        if (overlay == Overlay.NONE && !editingAddress) {
            val toolbarModifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
            CompositionLocalProvider(LocalGlassBackdrop provides glassBackdropState) {
                BrowserToolbar(
                    tab = tab,
                    collapseFraction = collapseFraction,
                    onBack = { activeWebView()?.let { if (it.canGoBack()) it.goBack() } },
                    onForward = { activeWebView()?.let { if (it.canGoForward()) it.goForward() } },
                    onShare = { shareCurrent() },
                    onReloadOrStop = {
                        val wv = activeWebView()
                        if (tab?.isLoading == true) wv?.stopLoading() else wv?.reload()
                    },
                    onAddressTap = { editingAddress = true },
                    onMore = { showMore = true },
                    onBookmarks = { libraryTab = LibraryTab.BOOKMARKS; overlay = Overlay.LIBRARY },
                    onTabs = { captureCurrentThumbnail(); overlay = Overlay.TABS },
                    modifier = toolbarModifier,
                )
            }
        }
    }

    // ---------- More menu popover ----------
    if (showMore) {
        MoreMenu(
            tab = tab,
            isBookmarked = tab?.let { viewModel.isBookmarked(it.url) } == true,
            onShare = { showMore = false; shareCurrent() },
            onBookmarks = { showMore = false; libraryTab = LibraryTab.BOOKMARKS; overlay = Overlay.LIBRARY },
            onAllTabs = { showMore = false; captureCurrentThumbnail(); overlay = Overlay.TABS },
            onNewTab = { showMore = false; viewModel.newTab(private = false); editingAddress = true },
            onNewPrivateTab = { showMore = false; viewModel.newTab(private = true); editingAddress = true },
            onAddReadingList = {
                showMore = false
                tab?.let { viewModel.addToReadingList(it.url, it.title) }
            },
            onToggleBookmark = {
                showMore = false
                tab?.let { viewModel.toggleBookmark(it.url, it.title) }
            },
            onDownloads = { showMore = false; overlay = Overlay.DOWNLOADS },
            onHistory = { showMore = false; libraryTab = LibraryTab.HISTORY; overlay = Overlay.LIBRARY },
            onSettings = { showMore = false; overlay = Overlay.SETTINGS },
            onDismiss = { showMore = false },
        )
    }

    // ---------- Address editor ----------
    AnimatedVisibility(
        visible = editingAddress,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.zIndex(2f),
    ) {
        AddressEditor(
            initialUrl = tab?.url?.takeIf { it.isNotBlank() } ?: "",
            searchEngineLabel = viewModel.settings.searchEngine.label,
            history = viewModel.history,
            bookmarks = viewModel.bookmarks,
            onSubmit = { text ->
                editingAddress = false
                viewModel.submitQuery(text)
            },
            onDismiss = { editingAddress = false },
        )
    }

    // ---------- Full-screen sheets ----------
    AnimatedVisibility(
        visible = overlay == Overlay.TABS,
        enter = fadeIn() + slideInVertically { it / 6 },
        exit = fadeOut() + slideOutVertically { it / 6 },
        modifier = Modifier.zIndex(3f),
    ) {
        TabsOverview(
            tabs = viewModel.visibleTabs,
            currentTabId = viewModel.currentTabId,
            isPrivate = viewModel.isPrivateMode,
            onSelect = { id -> viewModel.selectTab(id); overlay = Overlay.NONE },
            onClose = { id -> viewModel.closeTab(id) },
            onNewTab = { viewModel.newTab(); overlay = Overlay.NONE; editingAddress = true },
            onTogglePrivate = { priv -> viewModel.switchPrivateMode(priv) },
            onDone = { overlay = Overlay.NONE },
        )
    }

    AnimatedVisibility(
        visible = overlay == Overlay.LIBRARY,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.zIndex(3f),
    ) {
        LibrarySheet(
            initialTab = libraryTab,
            bookmarks = viewModel.bookmarks,
            readingList = viewModel.readingList,
            history = viewModel.history,
            onOpen = { url -> overlay = Overlay.NONE; tab?.let { viewModel.loadInTab(it, url) } ?: viewModel.newTab(url = url) },
            onRemoveBookmark = viewModel::removeBookmark,
            onRemoveReading = viewModel::removeFromReadingList,
            onRemoveHistory = viewModel::removeHistory,
            onClearHistory = viewModel::clearHistory,
            onDismiss = { overlay = Overlay.NONE },
        )
    }

    AnimatedVisibility(
        visible = overlay == Overlay.DOWNLOADS,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.zIndex(3f),
    ) {
        DownloadsSheet(
            downloads = viewModel.downloads,
            onClear = viewModel::clearDownloads,
            onDismiss = { overlay = Overlay.NONE },
        )
    }

    AnimatedVisibility(
        visible = overlay == Overlay.SETTINGS,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.zIndex(3f),
    ) {
        SettingsSheet(
            settings = viewModel.settings,
            onUpdate = { transform -> viewModel.updateSettings(transform) },
            onDismiss = { overlay = Overlay.NONE },
        )
    }
}
