package com.petersamokhin.vksdk.android.auth.error

/**
 * Thrown if the official VK App is not installed
 */
public class VkAppMissingException(
    override val message: String? = null,
    override val cause: Throwable? = null
): VkAuthException(message, cause)