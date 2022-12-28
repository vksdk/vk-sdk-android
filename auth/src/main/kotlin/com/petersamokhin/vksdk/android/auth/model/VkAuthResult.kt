package com.petersamokhin.vksdk.android.auth.model

/**
 * Authorization result based on the type of auth and on successfullness.
 */
public sealed class VkAuthResult {
    /**
     * Successful result for the request with `response_type=access_token`
     * See: https://vk.com/dev/implicit_flow_user
     *
     * @param accessToken Access token
     * @param expiresIn Expire time of the access token; `0` == `forever`
     * @param userId User ID
     * @param email User email. Only provided if this scope was requested and if user had not disallowed the access.
     * @param state An arbitrary string that is returned together with authorization result, only if was provided with the request.
     */
    public data class AccessToken(
        val accessToken: String,
        val expiresIn: Int,
        val userId: Int,
        val email: String?,
        val state: String?
    ): VkAuthResult()

    /**
     * Successful result for the request with `response_type=code`
     * See: https://vk.com/dev/authcode_flow_user
     *
     * @param code Code
     * @param state An arbitrary string that is returned together with authorization result, only if was provided with the request.
     */
    public data class Code(
        val code: String,
        val state: String?
    ): VkAuthResult()

    /**
     * Successful result for the request with `response_type=code`
     *
     * @param error Error from the response
     * @param description Error description from the response
     * @param exception If error occurred before the request
     */
    public data class Error(
        val error: String? = null,
        val description: String? = null,
        val reason: String? = null,
        val exception: Exception? = null
    ): VkAuthResult()
}