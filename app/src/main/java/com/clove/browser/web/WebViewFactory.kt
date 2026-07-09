package com.clove.browser.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.clove.browser.BrowserViewModel
import com.clove.browser.TabState

/**
 * Builds a fully-configured [WebView] wired to a [TabState]. Each tab keeps its own
 * WebView instance so back/forward history and scroll position survive tab switches.
 *
 * [onScroll] receives the vertical delta (dy) on every scroll so the chrome can
 * collapse (scrolling down) / expand (scrolling up), like iOS 26 Safari.
 */
@SuppressLint("SetJavaScriptEnabled")
fun createBrowserWebView(
    context: Context,
    tab: TabState,
    viewModel: BrowserViewModel,
    onScroll: (dy: Int, scrollY: Int) -> Unit,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        overScrollMode = View.OVER_SCROLL_NEVER
        isVerticalScrollBarEnabled = false
        // Force a hardware (GPU) layer so rendering/compositing stays off the CPU.
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Private tabs don't persist cookies to disk.
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, !tab.isPrivate)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = !tab.isPrivate
            databaseEnabled = !tab.isPrivate
            cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mediaPlaybackRequiresUserGesture = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            // A modern mobile UA so sites serve their phone layout.
            userAgentString = userAgentString.replace("; wv", "")
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                tab.isLoading = true
                tab.progress = 0.05f
                if (url != null) tab.url = url
                tab.canGoBack = view.canGoBack()
                tab.canGoForward = view.canGoForward()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                tab.isLoading = false
                tab.progress = 1f
                if (url != null) tab.url = url
                tab.canGoBack = view.canGoBack()
                tab.canGoForward = view.canGoForward()
                viewModel.recordVisit(url ?: tab.url, view.title ?: tab.title)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("http://") || url.startsWith("https://")) return false
                // tel:, mailto:, intent:, market:, etc. -> hand off to the system.
                return try {
                    val intent = if (url.startsWith("intent:")) {
                        Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    } else {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    true
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                tab.progress = newProgress / 100f
                tab.isLoading = newProgress < 100
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                if (!title.isNullOrBlank()) tab.title = title
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
                if (icon != null) tab.favicon = icon
            }
        }

        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            viewModel.enqueueDownload(url, userAgent, contentDisposition, mimeType)
        }

        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            onScroll(scrollY - oldScrollY, scrollY)
        }
    }
}

/** Renders the WebView's current frame to a bitmap for the tab-switcher thumbnail. */
fun WebView.captureThumbnail(): Bitmap? {
    if (width <= 0 || height <= 0) return null
    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        bitmap
    } catch (e: Exception) {
        null
    }
}
