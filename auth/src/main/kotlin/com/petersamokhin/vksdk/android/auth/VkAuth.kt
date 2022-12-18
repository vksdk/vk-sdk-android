package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CheckResult
import androidx.core.os.bundleOf
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAppMissingException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult
import java.util.*

/**
 * VK authorization handler.
 */
object VkAuth {
    private const val LOG_TAG = "vksdk.android.auth"

    private const val VK_APP_AUTH_ACTION = "com.vkontakte.android.action.SDK_AUTH"
    internal const val VK_AUTH_CODE = 1337
    private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"
    const val VK_API_VERSION_DEFAULT = 5.103
    const val VK_REDIRECT_URI_DEFAULT = "https://oauth.vk.com/blank.html"

    private const val INFO_RESPONSE_TYPE_NOT_SUPPORTED = "Specifying the response_type is not available with the official VK App, so it can not be used"

    private const val VK_EXTRA_CLIENT_ID = "client_id"
    private const val VK_EXTRA_REVOKE = "revoke"
    private const val VK_EXTRA_SCOPE = "scope"
    private const val VK_EXTRA_REDIRECT_URI = "redirect_uri"

    private const val VK_EXTRA_RESPONSE_TYPE = "response_type"
    private const val VK_EXTRA_DISPLAY = "display"
    private const val VK_STATE = "state"

    private val resultLaunchers = WeakHashMap<ComponentActivity, ActivityResultLauncher<Intent>>()

    /**
     * Register the given activity for auth result.
     * See [ComponentActivity.registerForActivityResult]
     * Result will be returned to [listener]
     */
    @JvmStatic
    fun register(activity: ComponentActivity, listener: ResultListener) {
        val resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            VkResultParser.parse(VK_AUTH_CODE, it.resultCode, it.data)?.also(listener::onResult)
        }
        resultLaunchers[activity] = resultLauncher
    }

    /**
     * Register the given activity for auth result.
     * See [ComponentActivity.registerForActivityResult]
     * Result will be returned to [listener]
     */
    @JvmStatic
    fun register(activity: ComponentActivity, listener: (VkAuthResult) -> Unit) {
        val resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            VkResultParser.parse(VK_AUTH_CODE, it.resultCode, it.data)?.also { result -> listener.invoke(result) }
        }
        resultLaunchers[activity] = resultLauncher
    }

    /**
     * Checks is the official VK app installed
     * to be able to authorize through the app without the WebView
     */
    @Suppress("DEPRECATION")
    @JvmStatic
    fun isVkAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(VK_APP_PACKAGE_ID, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * The authorization result returned by the activity with WebView or from VK App
     * can be parsed using this method.
     *
     * @param requestCode See [Activity.onActivityResult]
     * @param resultCode See [Activity.onActivityResult]
     * @param data See [Activity.onActivityResult]
     * @return Parsed authorization result, null if requestCode is wrong
     */
    @JvmStatic
    @CheckResult
    fun parseResult(requestCode: Int, resultCode: Int, data: Intent?): VkAuthResult? = VkResultParser.parse(requestCode, resultCode, data)

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     */
    @JvmStatic
    fun loginWithApp(
        activity: ComponentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithApp(
            activity = activity,
            authParams = AuthParams(
                clientId = clientId,
                responseType = responseType,
                scopes = scopes,
                redirectUri = redirectUri,
                display = display,
                state = state,
                revoke = revoke,
                apiVersion = apiVersion
            )
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     *
     * @return Disposable item: clear listeners when it needed
     */
    @JvmStatic
    fun loginWithApp(
        activity: ComponentActivity,
        authParams: AuthParams
    ) {
        if (isVkAppInstalled(activity))
            throw VkAppMissingException()

        launchLogin(activity, intentVkApp(authParams))
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     */
    @JvmStatic
    fun loginWithWebView(
        activity: ComponentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithWebView(
            activity = activity,
            authParams = AuthParams(
                clientId = clientId,
                responseType = responseType,
                scopes = scopes,
                redirectUri = redirectUri,
                display = display,
                state = state,
                revoke = revoke,
                apiVersion = apiVersion
            )
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     *
     * @return Disposable item: clear listeners when it needed
     */
    @JvmStatic
    fun loginWithWebView(
        activity: ComponentActivity,
        authParams: AuthParams
    ) {
        launchLogin(activity, VkAuthActivity.intent(activity, authParams))
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     */
    @JvmStatic
    fun login(
        activity: ComponentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        login(
            activity = activity,
            authParams = AuthParams(
                clientId = clientId,
                responseType = responseType,
                scopes = scopes,
                redirectUri = redirectUri,
                display = display,
                state = state,
                revoke = revoke,
                apiVersion = apiVersion
            )
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     */
    @JvmStatic
    fun login(
        activity: ComponentActivity,
        authParams: AuthParams
    ) {
        when (authParams.responseType) {
            ResponseType.AccessToken -> {
                val intent = if (isVkAppInstalled(activity))
                    intentVkApp(authParams)
                else
                    VkAuthActivity.intent(activity, authParams)

                launchLogin(activity, intent)
            }
            ResponseType.Code -> {
                Log.w(LOG_TAG, INFO_RESPONSE_TYPE_NOT_SUPPORTED)
                launchLogin(activity, VkAuthActivity.intent(activity, authParams))
            }
        }
    }

    @JvmStatic
    private fun launchLogin(activity: ComponentActivity, intent: Intent) {
        resultLaunchers[activity]?.launch(intent)
    }

    private fun intentVkApp(authParams: AuthParams) = Intent(VK_APP_AUTH_ACTION).apply {
        setPackage(VK_APP_PACKAGE_ID)
        putExtras(authParams.asBundle(false))
    }

    /**
     * All necessary auth params
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    data class AuthParams(
        val clientId: Int,
        val responseType: ResponseType,
        val scope: String = "",
        val redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        val display: Display = Display.Mobile,
        val state: String = "",
        val revoke: Boolean = true,
        val apiVersion: Double = 5.103
    ) {
        constructor(
            clientId: Int,
            responseType: ResponseType,
            scopes: List<Scope> = listOf(),
            redirectUri: String = VK_REDIRECT_URI_DEFAULT,
            display: Display = Display.Mobile,
            state: String = "",
            revoke: Boolean = true,
            apiVersion: Double = 5.103
        ) : this(
            clientId = clientId,
            responseType = responseType,
            scope = scopes.sumBy { it.intValue }.toString().let { if (it == "0") "" else it },
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        )

        /**
         * Bundle used for the VK App authorization
         *
         * @param withIgnored Some parameters are ignored by the VK App and may take unknown effect.
         * @return Bundle with the params
         */
        @CheckResult
        fun asBundle(withIgnored: Boolean): Bundle {
            return bundleOf(
                VK_EXTRA_CLIENT_ID to clientId,
                VK_EXTRA_REVOKE to revoke,
                VK_EXTRA_REDIRECT_URI to redirectUri
            ).apply {
                if (scope.isNotEmpty()) {
                    putString(VK_EXTRA_SCOPE, scope)
                }

                if (withIgnored) {
                    putString(VK_EXTRA_RESPONSE_TYPE, responseType.stringValue)
                    putString(VK_EXTRA_DISPLAY, display.stringValue)
                    putString(VK_STATE, state)
                }
            }
        }

        /**
         * Query used for manual authorization using the web page shown in the WebView
         *
         * @return client_id=...&scope=..., etc.
         */
        @CheckResult
        fun asQuery(): String {
            val map = mutableMapOf(
                "client_id" to clientId.toString(),
                "redirect_uri" to redirectUri,
                "response_type" to responseType.stringValue,
                "display" to display.stringValue,
                "v" to apiVersion.toString()
            )

            if (scope.isNotEmpty()) {
                map["scope"] = scope
            }

            if (revoke) {
                map["revoke"] = "1"
            }

            if (state.isNotEmpty()) {
                map["state"] = state
            }

            return map.map { (k, v) -> "$k=$v" }.joinToString("&")
        }
    }

    /**
     * Response type: access_token or code
     *
     * See:
     */
    enum class ResponseType(val stringValue: String) {
        /**
         * See: https://vk.com/dev/implicit_flow_user
         */
        AccessToken("token"),

        /**
         * See: https://vk.com/dev/authcode_flow_user
         */
        Code("code")
    }

    /**
     * Display type of the authorization page.
     * Prefer to use [Mobile].
     * [Android] and [Ios] are private type of the VK official clients.
     */
    enum class Display(val stringValue: String) {
        /**
         * Mobile page without the JavaScript.
         * Most preferred variant.
         */
        Mobile("mobile"),

        /**
         * New window
         */
        Page("page"),

        /**
         * Popup
         */
        Popup("popup"),

        /**
         * Page which looks like part of the official VK Android app.
         * Private and not documented
         */
        Android("android"),

        /**
         * Page which looks like part of the official VK iOS app.
         * Private and not documented
         */
        Ios("ios")
    }

    /**
     * Access token scope permissions
     * See: https://vk.com/dev/permissions
     */
    enum class Scope(val intValue: Int) {
        Notify(1), Friends(2), Photos(4), Audio(8),
        Video(16), Stories(64), Pages(128), LeftMenuLinks(256),
        Status(1024), Notes(2048), Messages(4096), Wall(8192),
        Ads(32768), Offline(65536), Docs(131072), Groups(262144),
        Notifications(524288), Stats(1048576), Email(4194304), Market(134217728)
    }

    /**
     * Inline listener for the auth result
     */
    interface ResultListener {
        /**
         * Handle the authorization result
         */
        fun onResult(result: VkAuthResult)
    }
}
