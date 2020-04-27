package com.petersamokhin.vksdk.android.auth

import org.junit.Assert.*
import org.junit.Test

class VkAuthParamsTest {
    @Test
    fun `should build query with default params`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            "offline"
        )
        val expectedUri = "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=${VkAuth.VK_API_VERSION_DEFAULT}&scope=offline&revoke=1"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    fun `should build query without revoke`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            "offline",
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            false, 5.103
        )
        val expectedUri = "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.103&scope=offline&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    fun `should build query with list of scopes`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            listOf(VkAuth.Scope.Offline),
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            false, 5.103
        )
        val expectedUri = "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.103&scope=65536&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    fun `should build query with string scope`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            "offline",
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            true, 5.103
        )
        val expectedUri = "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.103&scope=offline&revoke=1&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }
}