package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.CheckResult
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAppMissingException
import com.petersamokhin.vksdk.android.auth.hidden.ActivityResultListener
import com.petersamokhin.vksdk.android.auth.hidden.HiddenFragment
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult

/**
 * VK authorization handler.
 */
object VkAuth {
    private const val LOG_TAG = "vksdk.android.auth"
    private const val FRAGMENT_TAG = "vksdk.android.auth.fragment"

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

    /**
     * Checks is the official VK app installed
     * to be able to authorize through the app without the WebView
     */
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
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithApp(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: ResultListener
    ): DisposableItem {
        return loginWithApp(fragmentActivity, AuthParams(
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion
        ), listener)
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithApp(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        return loginWithApp(fragmentActivity, AuthParams(
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion
        ), listener)
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     *
     * @return Disposable item: clear listeners when it needed
     */
    @JvmStatic
    @CheckResult
    fun loginWithApp(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        if (isVkAppInstalled(fragmentActivity))
            throw VkAppMissingException()

        return loginHidden(
            fragmentActivity,
            intentVkApp(authParams),
            object : ResultListener {
                override fun onResult(result: VkAuthResult) = listener(result)
            }
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithApp(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: ResultListener
    ): DisposableItem {
        if (isVkAppInstalled(fragmentActivity))
            throw VkAppMissingException()

        return loginHidden(
            fragmentActivity,
            intentVkApp(authParams),
            listener
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithWebView(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: ResultListener
    ): DisposableItem {
        return loginWithWebView(fragmentActivity, AuthParams(
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion
        ), listener)
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithWebView(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        return loginWithWebView(fragmentActivity, AuthParams(
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion
        ), listener)
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     *
     * @return Disposable item: clear listeners when it needed
     */
    @JvmStatic
    @CheckResult
    fun loginWithWebView(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        return loginHidden(
            fragmentActivity,
            VkAuthActivity.intent(fragmentActivity, authParams),
            object : ResultListener {
                override fun onResult(result: VkAuthResult) = listener(result)
            }
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun loginWithWebView(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: ResultListener
    ): DisposableItem {
        return loginHidden(
            fragmentActivity,
            VkAuthActivity.intent(fragmentActivity, authParams),
            listener
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun login(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: ResultListener
    ): DisposableItem {
        return login(fragmentActivity, AuthParams(
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion
        ), listener)
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun login(
        fragmentActivity: FragmentActivity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        return login(
            fragmentActivity,
            clientId,
            responseType,
            scopes,
            redirectUri,
            display,
            state,
            revoke,
            apiVersion,
            object : ResultListener {
                override fun onResult(result: VkAuthResult) = listener(result)
            }
        )
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun login(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: ResultListener
    ): DisposableItem {
        return when (authParams.responseType) {
            ResponseType.AccessToken -> {
                val intent = if (isVkAppInstalled(fragmentActivity))
                    intentVkApp(authParams)
                else
                    VkAuthActivity.intent(fragmentActivity, authParams)

                loginHidden(
                    fragmentActivity,
                    intent,
                    listener
                )
            }
            ResponseType.Code -> {
                Log.w(LOG_TAG, INFO_RESPONSE_TYPE_NOT_SUPPORTED)
                loginHidden(
                    fragmentActivity,
                    VkAuthActivity.intent(fragmentActivity, authParams),
                    listener
                )
            }
        }
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [listener]
     */
    @JvmStatic
    @CheckResult
    fun login(
        fragmentActivity: FragmentActivity,
        authParams: AuthParams,
        listener: (VkAuthResult) -> Unit
    ): DisposableItem {
        return login(fragmentActivity, authParams, object : ResultListener {
            override fun onResult(result: VkAuthResult) = listener(result)
        })
    }

    @JvmStatic
    @CheckResult
    private fun loginHidden(
        fragmentActivity: FragmentActivity,
        intent: Intent,
        listener: ResultListener
    ): DisposableItem {
        return object : DisposableItem {
            init {
                val item = HiddenFragment.newInstance(
                    intent,
                    VK_AUTH_CODE,
                    object : ActivityResultListener {
                        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                            VkResultParser.parse(requestCode, resultCode, data)?.also(listener::onResult)

                            fragmentActivity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)?.also {
                                fragmentActivity.supportFragmentManager
                                    .beginTransaction()
                                    .remove(it)
                                    .commitAllowingStateLoss()
                            }
                        }
                    }
                )

                fragmentActivity.supportFragmentManager
                    .beginTransaction()
                    .add(item, FRAGMENT_TAG)
                    .commitAllowingStateLoss()
            }

            override fun dispose() {
                HiddenFragment.clear()
            }
        }
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult] in both cases.
     */
    @JvmStatic
    fun login(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        login(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scopes.sumBy { it.intValue }.toString(),
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult] in both cases.
     */
    @JvmStatic
    fun login(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scope: String = "",
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        login(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scope,
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult] in both cases.
     */
    @JvmStatic
    fun login(activity: Activity, authParams: AuthParams) {
        when (authParams.responseType) {
            ResponseType.AccessToken -> {
                if (isVkAppInstalled(activity)) {
                    loginWithApp(activity, authParams)
                } else {
                    loginWithWebView(activity, authParams)
                }
            }
            ResponseType.Code -> {
                Log.w(LOG_TAG, INFO_RESPONSE_TYPE_NOT_SUPPORTED)
                loginWithWebView(activity, authParams)
            }
        }
    }

    /**
     * Login with VK using the available methods:
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithWebView(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithWebView(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scopes.sumBy { it.intValue }.toString(),
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithWebView(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scope: String = "",
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithWebView(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scope,
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if the VK App is not installed, the exception will be thrown
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithApp(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scopes: List<Scope> = listOf(),
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithApp(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scopes.sumBy { it.intValue }.toString(),
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if the VK App is not installed, the exception will be thrown
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithApp(
        activity: Activity,
        clientId: Int,
        responseType: ResponseType,
        scope: String = "",
        redirectUri: String = VK_REDIRECT_URI_DEFAULT,
        display: Display = Display.Mobile,
        state: String = "",
        revoke: Boolean = true,
        apiVersion: Double = VK_API_VERSION_DEFAULT
    ) {
        loginWithApp(activity, AuthParams(
            clientId = clientId,
            responseType = responseType,
            scope = scope,
            redirectUri = redirectUri,
            display = display,
            state = state,
            revoke = revoke,
            apiVersion = apiVersion
        ))
    }

    /**
     * Login with VK using the available methods:
     * - if the VK App is not installed, the exception will be thrown
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithApp(
        activity: Activity,
        authParams: AuthParams
    ) {
        if (!isVkAppInstalled(activity)) {
            throw VkAppMissingException()
        }

        activity.startActivityForResult(intentVkApp(authParams), VK_AUTH_CODE)
    }

    /**
     * Login with VK using the available methods:
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [activity]'s method [Activity.onActivityResult]
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @JvmStatic
    fun loginWithWebView(
        activity: Activity,
        authParams: AuthParams
    ) {
        activity.startActivityForResult(VkAuthActivity.intent(activity, authParams), VK_AUTH_CODE)
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

    /**
     * Some disposable item
     */
    interface DisposableItem {
        /**
         * Use to clear listeners
         */
        fun dispose()
    }
}
