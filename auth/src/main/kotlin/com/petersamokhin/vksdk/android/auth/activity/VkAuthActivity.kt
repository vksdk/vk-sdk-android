package com.petersamokhin.vksdk.android.auth.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.petersamokhin.vksdk.android.auth.R
import com.petersamokhin.vksdk.android.auth.VkAuth
import com.petersamokhin.vksdk.android.auth.view.VkAuthWebView

/**
 * Activity used to show the web page in the WebView
 */
internal class VkAuthActivity : AppCompatActivity() {
    private val webView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<VkAuthWebView>(R.id.webView)
    }
    private val progress by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<View>(R.id.progress)
    }

    private lateinit var lastQuery: String
    private lateinit var lastRedirectUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vk_auth)

        lastQuery = intent.getStringExtra(EXTRA_AUTH_QUERY) ?: return finish()
        lastRedirectUri = intent.getStringExtra(EXTRA_AUTH_REDIRECT_URI) ?: return finish()

        loadUrl("$VK_AUTH_BASE_URL?$lastQuery")
    }

    private fun setResult(url: String) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_AUTH_RESULT, url))
        finish()
    }

    private fun loadUrl(authUrl: String) {
        webView.apply {
            setResultUrl(lastRedirectUri)
            setAuthResultUrlCallback(::setResult)
            setOnPageShownListener {
                showWebView()
            }
            setErrorCallback {
                setResult(Activity.RESULT_CANCELED, Intent().putExtra("error_description", it.orEmpty()))
                finish()
            }
            try {
                loadUrl(authUrl)
            } catch (e: Exception) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun showWebView() {
        progress.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        internal const val EXTRA_AUTH_RESULT = "EXTRA_AUTH_RESULT"
        internal const val EXTRA_AUTH_QUERY = "EXTRA_AUTH_QUERY"
        internal const val EXTRA_AUTH_REDIRECT_URI = "EXTRA_AUTH_REDIRECT_URI"
        private const val VK_AUTH_BASE_URL = "https://oauth.vk.com/authorize"

        @JvmStatic
        fun intent(activity: Activity, authParams: VkAuth.AuthParams) =
            Intent(activity, VkAuthActivity::class.java)
                .putExtra(EXTRA_AUTH_QUERY, authParams.asQuery())
                .putExtra(EXTRA_AUTH_REDIRECT_URI, authParams.redirectUri)
    }
}