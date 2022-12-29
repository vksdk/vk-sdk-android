package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CheckResult
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.bundleOf
import com.petersamokhin.vksdk.android.auth.VkAuth.Display.Android
import com.petersamokhin.vksdk.android.auth.VkAuth.Display.Ios
import com.petersamokhin.vksdk.android.auth.VkAuth.Display.Mobile
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity.Companion.VK_AUTH_BASE_URL
import com.petersamokhin.vksdk.android.auth.error.VkAuthException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult
import com.petersamokhin.vksdk.android.auth.utils.toMap
import kotlinx.parcelize.Parcelize
import java.util.WeakHashMap

/**
 * VK authorization handler.
 */
public object VkAuth {
    private const val LOG_TAG = "vksdk.android.auth"

    private const val VK_APP_AUTH_ACTION = "com.vkontakte.android.action.SDK_AUTH"
    private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"
    public const val VK_API_VERSION_DEFAULT: String = "5.113"
    public const val VK_REDIRECT_URI_DEFAULT: String = "https://oauth.vk.com/blank.html"

    private const val INFO_RESPONSE_TYPE_NOT_SUPPORTED =
        "Specifying the response_type is not available with the official VK App, so it can not be used"

    private const val VK_EXTRA_CLIENT_ID = "client_id"
    private const val VK_EXTRA_REVOKE = "revoke"
    private const val VK_EXTRA_SCOPE = "scope"
    private const val VK_EXTRA_REDIRECT_URI = "redirect_uri"

    private const val VK_EXTRA_RESPONSE_TYPE = "response_type"
    private const val VK_EXTRA_DISPLAY = "display"
    private const val VK_STATE = "state"

    private const val SERVICE_ACTION = "android.support.customtabs.action.CustomTabsService"
    private const val CHROME_PACKAGE = "com.android.chrome"

    private var resultLaunchers: MutableMap<ComponentActivity, ActivityResultLauncher<Intent>> = WeakHashMap()

    /**
     * Register the given activity for auth result.
     * See [ComponentActivity.registerForActivityResult]
     * Result will be returned to [listener]
     *
     * Note: this must be called in [activity] before [Activity.onCreate]:
     * [See more](https://developer.android.com/training/basics/intents/result#register)
     */
    @JvmStatic
    public fun register(
        activity: ComponentActivity,
        overrideLaunchersMap: MutableMap<ComponentActivity, ActivityResultLauncher<Intent>>? = null,
        listener: (Result<VkAuthResult>) -> Unit,
    ) {
        if (overrideLaunchersMap != null) {
            resultLaunchers = overrideLaunchersMap.also { map -> map.putAll(resultLaunchers) }
        }

        activity.addOnNewIntentListener { intent ->
            resultLaunchers.remove(activity)

            try {
                listener(
                    Result.success(VkResultParser.parseCustomTabs(intent = intent))
                )
            } catch (e: Throwable) {
                listener(
                    Result.failure(
                        VkAuthException("Failed to parse the VK auth result: $intent, extras=${intent.extras?.toMap()}")
                    )
                )
            }
        }

        val resultLauncher = activity.registerForActivityResult(
            /* contract = */ ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val vkResult = try {
                VkResultParser.parse(
                    resultCode = result.resultCode,
                    intent = result.data,
                )
            } catch (e: Throwable) {
                listener(Result.failure(e))
                null
            } finally {
                resultLaunchers.remove(activity)
            }

            if (vkResult != null) {
                listener(Result.success(vkResult))
            } else {
                listener(Result.failure(VkAuthException("Failed to parse the VK auth result: $result")))
            }
        }
        resultLaunchers[activity] = resultLauncher
    }

    /**
     * Checks is the official VK app installed
     * to be able to authorize through the app without the WebView
     */
    @[JvmStatic Suppress("DEPRECATION")]
    public fun isVkAppInstalled(context: Context): Boolean {
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
     * @param resultCode See [Activity.onActivityResult]
     * @param data See [Activity.onActivityResult]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    public fun parseResult(resultCode: Int, data: Intent?): VkAuthResult =
        VkResultParser.parse(resultCode, data)

    /**
     * The authorization result returned by Custom Tabs can be parsed here.
     *
     * @param data See [Activity.onNewIntent]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    public fun parseCustomTabsResult(data: Intent?): VkAuthResult =
        VkResultParser.parseCustomTabs(data)

    /**
     * Login with VK using the available methods:
     * - if VK App is installed, it will be used
     * - if you need the `code` instead of `access_token`, WebView the only way to retrieve it
     * - result will be returned to [register]ed listener.
     *
     * Note: with [AuthMode.Auto] or [AuthMode.RequireWeb] the CustomTabs may be used.
     * For this, a custom [AuthParams.redirectUri] is required, which also must be specified in the AndroidManifest.xml
     */
    @JvmStatic
    public fun login(
        activity: ComponentActivity,
        authParams: AuthParams,
        mode: AuthMode = AuthMode.Auto,
    ) {
        when (authParams.responseType) {
            ResponseType.AccessToken -> {
                val intent = when (mode) {
                    AuthMode.RequireApp -> {
                        if (!isVkAppInstalled(activity)) {
                            throw VkAuthException("VK app is required but is not available")
                        }

                        intentVkApp(authParams)
                    }

                    AuthMode.RequireWeb -> {
                        when {
                            activity.customTabsSupported() -> {
                                resultLaunchers.remove(activity)
                                activity.startActivity(loadCustomTabsAuthUrlIntent(authParams.asQuery()))
                                null
                            }

                            else -> {
                                VkAuthActivity.intent(activity, authParams)
                            }
                        }
                    }

                    AuthMode.RequireWebView -> {
                        VkAuthActivity.intent(activity, authParams)
                    }

                    AuthMode.Auto -> {
                        when {
                            isVkAppInstalled(activity) -> {
                                intentVkApp(authParams)
                            }

                            activity.customTabsSupported() -> {
                                resultLaunchers.remove(activity)
                                activity.startActivity(loadCustomTabsAuthUrlIntent(authParams.asQuery()))
                                null
                            }

                            else -> {
                                VkAuthActivity.intent(activity, authParams)
                            }
                        }
                    }
                }

                if (intent != null) {
                    launchLogin(
                        activity = activity,
                        intent = intent,
                    )
                }
            }

            ResponseType.Code -> {
                Log.w(LOG_TAG, INFO_RESPONSE_TYPE_NOT_SUPPORTED)

                when {
                    activity.customTabsSupported() -> {
                        resultLaunchers.remove(activity)
                        activity.startActivity(loadCustomTabsAuthUrlIntent(authParams.asQuery()))
                    }

                    else -> {
                        launchLogin(
                            activity = activity,
                            intent = VkAuthActivity.intent(activity, authParams),
                        )
                    }
                }
            }
        }
    }

    private fun loadCustomTabsAuthUrlIntent(query: String): Intent =
        CustomTabsIntent.Builder().build().intent
            .apply { data = Uri.parse("$VK_AUTH_BASE_URL?$query") }

    private fun Context.customTabsSupported(): Boolean {
        val serviceIntent = Intent(SERVICE_ACTION).apply { setPackage(CHROME_PACKAGE) }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentServices(serviceIntent, PackageManager.ResolveInfoFlags.of(0)).isNotEmpty()
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentServices(serviceIntent, 0).isNotEmpty()
        }
    }

    private fun intentVkApp(authParams: AuthParams) =
        Intent(VK_APP_AUTH_ACTION).apply {
            setPackage(VK_APP_PACKAGE_ID)
            putExtras(authParams.asBundle(withIgnored = false))
        }

    /**
     * All necessary auth params
     *
     * For the params description:
     * See: https://vk.com/dev/access_token
     * See: https://vk.com/dev/implicit_flow_user
     * See: https://vk.com/dev/authcode_flow_user
     */
    @Parcelize
    public data class AuthParams(
        val clientId: Int,
        val responseType: ResponseType,
        val scope: String = "",
        val redirectUri: String,
        val display: Display = Mobile,
        val state: String = "",
        val revoke: Boolean = true,
        val apiVersion: String = VK_API_VERSION_DEFAULT,
    ) : Parcelable {
        public constructor(
            clientId: Int,
            responseType: ResponseType,
            scopes: List<Scope> = listOf(),
            redirectUri: String,
            display: Display = Mobile,
            state: String = "",
            revoke: Boolean = true,
            apiVersion: String = VK_API_VERSION_DEFAULT
        ) : this(
            clientId = clientId,
            responseType = responseType,
            scope = scopes.sumOf<Scope>(Scope::intValue).toString().let { if (it == "0") "" else it },
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
        public fun asBundle(withIgnored: Boolean): Bundle {
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
        public fun asQuery(): String {
            val map = mutableMapOf(
                "client_id" to clientId.toString(),
                "redirect_uri" to redirectUri,
                "response_type" to responseType.stringValue,
                "display" to display.stringValue,
                "v" to apiVersion
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

    private fun launchLogin(activity: ComponentActivity, intent: Intent) {
        resultLaunchers[activity]?.launch(intent)
    }

    /**
     * Response type: access_token or code
     *
     * See:
     */
    public enum class ResponseType(public val stringValue: String) {
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
    public enum class Display(public val stringValue: String) {
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
    public enum class Scope(public val intValue: Int) {
        Notify(1), Friends(2), Photos(4), Audio(8),
        Video(16), Stories(64), Pages(128), LeftMenuLinks(256),
        Status(1024), Notes(2048), Messages(4096), Wall(8192),
        Ads(32768), Offline(65536), Docs(131072), Groups(262144),
        Notifications(524288), Stats(1048576), Email(4194304), Market(134217728)
    }

    /**
     * Use this as a param to [VkAuth.login] to specify the behavior
     */
    public enum class AuthMode {
        /**
         * VK official app will be used if [VkAuth.isVkAppInstalled],
         * otherwise an error will be thrown
         */
        RequireApp,

        /**
         * VK official app will not be used even if it is available, it will not be checked.
         * Otherwise, if the Chrome Custom Tabs are available, they will be used.
         * Otherwise, a WebView will be used.
         */
        RequireWeb,

        /**
         * VK official app will not be used even if it is available, it will not be checked.
         * Chrome Custom Tabs will not be used even if it is available, it will not be checked.
         * A WebView will be always used.
         */
        RequireWebView,

        /**
         * If the VK official app is available, it will be used.
         * Otherwise, if the Chrome Custom Tabs are available, they will be used.
         * Otherwise, a WebView will be used.
         *
         * Default behavior.
         */
        Auto
    }
}
