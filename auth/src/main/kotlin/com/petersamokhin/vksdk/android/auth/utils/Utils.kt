package com.petersamokhin.vksdk.android.auth.utils

import android.os.Bundle

/**
 * Convert Bundle to map
 */
internal fun Bundle.toMap(): Map<String, Any?> {
    return mutableMapOf<String, Any?>().apply {
        keySet().forEach { set(it, this@toMap.get(it)) }
    }
}