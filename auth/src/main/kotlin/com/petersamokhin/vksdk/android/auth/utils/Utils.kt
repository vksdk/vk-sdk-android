package com.petersamokhin.vksdk.android.auth.utils

import android.os.Bundle

/**
 * Convert Bundle to map
 */
@Suppress("DEPRECATION") // why the heck deprecating this with no alternative?
internal fun Bundle.toMap(): Map<String, Any?> =
    mutableMapOf<String, Any?>().also { map ->
        for (key in keySet()) {
            map[key] = get(key)
        }
    }