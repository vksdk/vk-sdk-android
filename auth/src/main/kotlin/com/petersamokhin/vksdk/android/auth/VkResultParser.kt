package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAuthCanceledException
import com.petersamokhin.vksdk.android.auth.error.VkAuthException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult
import com.petersamokhin.vksdk.android.auth.utils.toMap

/**
 * Parser of the VK result intents and URIs
 */
internal object VkResultParser {
    private const val VK_EXTRA_CODE = "code"
    private const val VK_EXTRA_ACCESS_TOKEN = "access_token"

    private const val VK_EXTRA_EXPIRES_IN = "expires_in"
    private const val VK_EXTRA_USER_ID = "user_id"

    private const val VK_EXTRA_ERROR = "error"
    private const val VK_EXTRA_ERROR_REASON = "error_reason"
    private const val VK_EXTRA_ERROR_DESCRIPTION = "error_description"

    private const val VK_EXTRA_EMAIL = "email"
    private const val VK_EXTRA_STATE = "state"

    private const val EMPTY_INT_PARAM = -1
    private const val EMPTY_STRING_PARAM = ""

    /**
     * The authorization result returned by the activity with WebView or from VK App
     * can be parsed using this method.
     *
     * @param resultCode See [Activity.onActivityResult]
     * @param intent See [Activity.onActivityResult]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    fun parse(resultCode: Int, intent: Intent?): VkAuthResult =
        parse(resultCode, intent?.extras?.toMap())

    @JvmStatic
    @CheckResult
    fun parse(resultCode: Int, extras: Map<String, Any?>?): VkAuthResult {
        if (extras == null)
            return VkAuthResult.Error(
                EMPTY_STRING_PARAM,
                EMPTY_STRING_PARAM,
                EMPTY_STRING_PARAM,
                exception = VkAuthCanceledException()
            )

        return when {
            extras.containsKey(VkAuthActivity.EXTRA_AUTH_RESULT) -> {
                @Suppress("RemoveExplicitTypeArguments")
                (extras.getValue(VkAuthActivity.EXTRA_AUTH_RESULT) as String).let<String, VkAuthResult> { resultUrlString ->
                    val params = try {
                        parseVkUri(resultUrlString)
                    } catch (e: Exception) {
                        return VkAuthResult.Error(
                            error = EMPTY_STRING_PARAM,
                            description = EMPTY_STRING_PARAM,
                            reason = EMPTY_STRING_PARAM,
                            exception = e,
                        )
                    }

                    when {
                        params.containsKey(VK_EXTRA_ACCESS_TOKEN) -> VkAuthResult.AccessToken(
                            params[VK_EXTRA_ACCESS_TOKEN] ?: EMPTY_STRING_PARAM,
                            params[VK_EXTRA_EXPIRES_IN]?.toIntOrNull() ?: EMPTY_INT_PARAM,
                            params[VK_EXTRA_USER_ID]?.toIntOrNull() ?: EMPTY_INT_PARAM,
                            params[VK_EXTRA_EMAIL] ?: EMPTY_STRING_PARAM,
                            params[VK_EXTRA_STATE] ?: EMPTY_STRING_PARAM
                        )

                        params.containsKey(VK_EXTRA_CODE) -> VkAuthResult.Code(
                            params[VK_EXTRA_CODE] ?: EMPTY_STRING_PARAM,
                            params[VK_EXTRA_STATE] ?: EMPTY_STRING_PARAM
                        )

                        else -> VkAuthResult.Error(
                            params[VK_EXTRA_ERROR] ?: EMPTY_STRING_PARAM,
                            params[VK_EXTRA_ERROR_DESCRIPTION] ?: EMPTY_STRING_PARAM,
                            params[VK_EXTRA_ERROR_REASON] ?: EMPTY_STRING_PARAM,
                            if (resultCode != Activity.RESULT_OK) VkAuthCanceledException() else null
                        )
                    }
                }
            }

            extras.containsKey(VK_EXTRA_ACCESS_TOKEN) -> {
                VkAuthResult.AccessToken(
                    extras[VK_EXTRA_ACCESS_TOKEN] as? String ?: EMPTY_STRING_PARAM,
                    extras[VK_EXTRA_EXPIRES_IN] as? Int ?: EMPTY_INT_PARAM,
                    extras[VK_EXTRA_USER_ID] as? Int ?: EMPTY_INT_PARAM,
                    extras[VK_EXTRA_EMAIL] as? String ?: EMPTY_STRING_PARAM,
                    extras[VK_EXTRA_STATE] as? String
                        ?: EMPTY_STRING_PARAM // state is never returned by the VK App
                )
            }

            else -> {
                VkAuthResult.Error(
                    extras[VK_EXTRA_ERROR] as? String,
                    extras[VK_EXTRA_ERROR_REASON] as? String,
                    extras[VK_EXTRA_ERROR_DESCRIPTION] as? String,
                    if (resultCode != Activity.RESULT_OK) VkAuthCanceledException() else null
                )
            }
        }
    }

    /**
     * The authorization result returned by Custom Tabs can be parsed here.
     *
     * @param intent See [Activity.onNewIntent]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    fun parseCustomTabs(intent: Intent?): VkAuthResult =
        parseCustomTabs(
            dataString = intent?.dataString,
            extras = intent?.extras?.toMap()
        )

    @JvmStatic
    @CheckResult
    fun parseCustomTabs(dataString: String?, extras: Map<String, Any?>?): VkAuthResult {
        val referrerResult: Result<VkAuthResult> = runCatching {
            val referrer = extras?.get(Intent.EXTRA_REFERRER)?.toString()
                ?: return@runCatching VkAuthResult.Error(
                    error = EMPTY_STRING_PARAM,
                    description = EMPTY_STRING_PARAM,
                    reason = EMPTY_STRING_PARAM,
                    exception = VkAuthException("Unknown custom tabs result: $extras")
                )

            val uri = Uri.parse(referrer)
            val redirectResult = Uri.decode(Uri.decode(uri.getQueryParameter("authorize_url"))) // lol

            return@runCatching parse(Activity.RESULT_OK, mapOf(VkAuthActivity.EXTRA_AUTH_RESULT to redirectResult))
        }

        val dataStringResult: Result<VkAuthResult> by lazy(LazyThreadSafetyMode.NONE) {
            runCatching {
                parse(Activity.RESULT_OK, mapOf(VkAuthActivity.EXTRA_AUTH_RESULT to dataString))
            }
        }

        return when {
            referrerResult.isSuccess && referrerResult.getOrThrow() !is VkAuthResult.Error -> {
                referrerResult.getOrThrow()
            }

            dataStringResult.isSuccess && dataStringResult.getOrThrow() !is VkAuthResult.Error -> {
                dataStringResult.getOrThrow()
            }

            else -> {
                VkAuthResult.Error(
                    error = EMPTY_STRING_PARAM,
                    description = EMPTY_STRING_PARAM,
                    reason = EMPTY_STRING_PARAM,
                    exception = VkAuthException("Unknown custom tabs result: $extras")
                )
            }
        }
    }

    @JvmStatic
    @VisibleForTesting
    internal fun parseVkUri(uri: String): Map<String, String> {
        return when {
            uri.isEmpty() -> throw IllegalArgumentException("VK auth result URL is empty")
            uri.startsWith('#') -> throw IllegalArgumentException("Unknown format of the VK auth result URL")
            uri.contains('#') -> {
                when (val indexOfSharp = uri.lastIndexOf('#')) {
                    uri.lastIndex -> mapOf()
                    uri.lastIndex - 1 -> mapOf(uri.substring(indexOfSharp + 1) to "")
                    else -> uri
                        .substring(uri.indexOf('#') + 1)
                        .split('&')
                        .associate { param ->
                            param.split('=')
                                .let {
                                    if (it.size == 1) {
                                        it[0] to ""
                                    } else {
                                        it[0] to it[1]
                                    }
                                }
                        }
                }

            }

            else -> throw IllegalArgumentException("Unknown format of the VK auth result URL")
        }
    }
}