package com.petersamokhin.vksdk.android.auth.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.petersamokhin.vksdk.android.auth.R

/**
 * Created: 2019-06-05 at 07:40
 * @author Peter Samokhin, https://petersamokhin.com/
 */
@Suppress("DEPRECATION", "OverridingDeprecatedMember")
@SuppressLint("SetJavaScriptEnabled")
internal class VkAuthWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : WebView(context, attrs, defStyle) {
    private var resultCallback: ResultUrlCallback? = null
    private var resultUrl: String? = null
    private var onPageShownListener: OnPageShownListener? = null
    private var errorCallback: ErrorCallback? = null
    private var lastUrl: String? = null

    init {
        webChromeClient = WebChromeClient()
        webViewClient = WebViewClient()
        overScrollMode = View.OVER_SCROLL_NEVER
        settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    private fun retry() {
        lastUrl?.also(::loadUrl)
    }

    override fun loadUrl(url: String) {
        lastUrl = url
        super.loadUrl(url)
    }

    fun setResultUrl(url: String) {
        resultUrl = url
    }

    fun setAuthResultUrlCallback(callback: ResultUrlCallback) {
        resultCallback = callback
    }

    fun setOnPageShownListener(listener: OnPageShownListener) {
        onPageShownListener = listener
    }

    fun setErrorCallback(listener: ErrorCallback) {
        errorCallback = listener
    }

    // androidx.webkit.WebViewClientCompat has a bug on some Android x86 devices
    // Fatal Exception: java.lang.IllegalArgumentException: reasonPhrase can't be empty.
    private inner class WebViewClient : android.webkit.WebViewClient() {
        private var isError = false

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
            if (!isError) onPageShownListener?.invoke()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (!isError) onPageShownListener?.invoke()
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = handleUrl(request.url.toString())

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = handleUrl(url)

        private fun handleUrl(url: String?): Boolean {
            url?.takeIf {
                it.startsWith(resultUrl ?: throw IllegalArgumentException("resultUr is null"))
            }?.also {
                resultCallback?.invoke(it)
                return true
            }

            return false
        }

        @RequiresApi(Build.VERSION_CODES.M)
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            view?.context?.also {
                showError(it, error?.description?.toString())
            }
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            view?.context?.also {
                showError(it, description)
            }
        }

        private fun showError(context: Context, description: String?) {
            isError = true
            try {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title_error)
                    .setMessage(description)
                    .setPositiveButton(context.getString(R.string.retry)) { d, _ ->
                        isError = false
                        retry()
                        d.dismiss()
                    }
                    .setNegativeButton(R.string.cancel) { d, _ ->
                        errorCallback?.invoke("description")
                        d.dismiss()
                    }
                    .show()
            } catch (e: Exception) {
                errorCallback?.invoke(e.message)
            }
        }
    }
}

internal typealias OnPageShownListener = () -> Unit
internal typealias ResultUrlCallback = (String) -> Unit
internal typealias ErrorCallback = (String?) -> Unit