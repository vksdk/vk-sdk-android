package com.petersamokhin.vksdk.android.auth.error

/**
 * When user cancelled the auth process.
 */
class VkAuthCanceledException(
    override val message: String? = null,
    override val cause: Throwable? = null
): VkAuthException(message, cause)