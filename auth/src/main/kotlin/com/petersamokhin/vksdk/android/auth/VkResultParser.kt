package com.petersamokhin.vksdk.android.auth

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAuthCanceledException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult

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
     * @param requestKey See [androidx.fragment.app.FragmentResultListener.onFragmentResult]
     * @param result See [androidx.fragment.app.FragmentResultListener.onFragmentResult]
     * @return Parsed authorization result
     */
    @JvmStatic
    @CheckResult
    fun parse(requestKey: String, result: Bundle?): VkAuthResult? {
        return when (requestKey) {
            VkAuth.VK_AUTH_CODE.toString() -> {
                if (result == null)
                    return VkAuthResult.Error(
                        EMPTY_STRING_PARAM,
                        EMPTY_STRING_PARAM,
                        EMPTY_STRING_PARAM,
                        exception = VkAuthCanceledException()
                    )

                when {
                    result.containsKey(VkAuthActivity.EXTRA_AUTH_RESULT) -> {
                        @Suppress("RemoveExplicitTypeArguments")
                        result.getString(VkAuthActivity.EXTRA_AUTH_RESULT)?.let<String, VkAuthResult> { resultUrlString ->
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
                                  null
                                )
                            }
                        }
                    }
                    result.containsKey(VK_EXTRA_ACCESS_TOKEN) -> {
                        VkAuthResult.AccessToken(
                            result.getString(VK_EXTRA_ACCESS_TOKEN, EMPTY_STRING_PARAM),
                            result.getInt(VK_EXTRA_EXPIRES_IN, EMPTY_INT_PARAM),
                            result.getInt(VK_EXTRA_USER_ID, EMPTY_INT_PARAM),
                            result.getString(VK_EXTRA_EMAIL, EMPTY_STRING_PARAM),
                            result.getString(VK_EXTRA_STATE,  EMPTY_STRING_PARAM) // state is never returned by the VK App
                        )
                    }
                    else -> {
                        VkAuthResult.Error(
                            result.getString(VK_EXTRA_ERROR, null),
                            result.getString(VK_EXTRA_ERROR_REASON, null),
                            result.getString(VK_EXTRA_ERROR_DESCRIPTION, null),
                            null
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