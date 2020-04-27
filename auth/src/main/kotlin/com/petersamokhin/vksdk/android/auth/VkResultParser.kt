package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAuthCanceledException
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
     * @param requestCode See [Activity.onActivityResult]
     * @param resultCode See [Activity.onActivityResult]
     * @param intent See [Activity.onActivityResult]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    fun parse(requestCode: Int, resultCode: Int, intent: Intent?): VkAuthResult? {
        return parse(requestCode, resultCode, intent?.extras?.toMap())
    }

    @JvmStatic
    @CheckResult
    fun parse(requestCode: Int, resultCode: Int, extras: Map<String, Any?>?): VkAuthResult? {
        return when (requestCode) {
            VkAuth.VK_AUTH_CODE -> {
                if (extras == null)
                    return VkAuthResult.Error(
                        EMPTY_STRING_PARAM,
                        EMPTY_STRING_PARAM,
                        EMPTY_STRING_PARAM,
                        exception = VkAuthCanceledException()
                    )

                when {
                    extras.containsKey(VkAuthActivity.EXTRA_AUTH_RESULT) -> {
                        @Suppress("RemoveExplicitTypeArguments")
                        (extras.getValue(VkAuthActivity.EXTRA_AUTH_RESULT) as String).let<String, VkAuthResult> { resultUrlString ->
                            val params = parseVkUri(resultUrlString)

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
            else -> null
        }
    }

    @JvmStatic
    @VisibleForTesting
    fun parseVkUri(uri: String): Map<String, String> {
        return when {
            uri.isEmpty() -> throw IllegalArgumentException("VK auth result URL is empty")
            uri.startsWith('#') -> throw IllegalArgumentException("Unknown format of the VK auth result URL")
            uri.contains('#') -> {
                when (val indexOfSharp = uri.lastIndexOf('#')) {
                    uri.lastIndex -> mapOf()
                    uri.lastIndex - 1 -> mapOf(uri.substring(indexOfSharp + 1) to "")
                    else -> uri.substring(uri.indexOf('#') + 1).split('&')
                        .map {
                            it.split('=')
                                .let {
                                    if (it.size == 1) {
                                        it[0] to ""
                                    } else {
                                        it[0] to it[1]
                                    }
                                }
                        }.toMap()
                }

            }
            else -> throw IllegalArgumentException("Unknown format of the VK auth result URL")
        }
    }
}