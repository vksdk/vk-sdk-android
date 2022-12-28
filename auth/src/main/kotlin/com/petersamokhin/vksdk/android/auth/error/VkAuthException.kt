package com.petersamokhin.vksdk.android.auth.error

/**
 * Base error
 */
public open class VkAuthException(
    override val message: String? = null,
    override val cause: Throwable? = null
): Exception(message, cause)