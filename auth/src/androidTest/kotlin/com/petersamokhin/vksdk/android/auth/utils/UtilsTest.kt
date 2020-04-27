package com.petersamokhin.vksdk.android.auth.utils

import androidx.core.os.bundleOf
import com.petersamokhin.vksdk.android.auth.VkAuth
import org.junit.Test

import org.junit.Assert.*

class UtilsTest {
    @Test
    fun shouldCorrectlyConvertEmptyBundle() {
        val bundle = bundleOf()
        val expectedMap = emptyMap<String, Any?>()
        assertEquals(expectedMap, bundle.toMap())
    }

    @Test
    fun shouldCorrectlyConvertBundleWithPrimitives() {
        val bundle = bundleOf("str" to "test1234", "bool" to true)
        val expectedMap = mapOf<String, Any?>("str" to "test1234", "bool" to true)
        assertEquals(expectedMap, bundle.toMap())
    }

    @Test
    fun shouldCorrectlyConvertBundleWithParcelables() {
        val authParams = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "https://oauth.vk.com/blank.html", "offline"
        )
        val bundle = bundleOf("str" to "test1234", "bool" to true, "item" to authParams)
        val expectedMap = mapOf<String, Any?>("str" to "test1234", "bool" to true, "item" to authParams)
        assertEquals(expectedMap, bundle.toMap())
    }
}