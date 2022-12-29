package com.petersamokhin.vksdk.android.auth

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
public class VkAuthParamsTest {
    @Test
    public fun `should build query with default params`() {
        val params = VkAuth.AuthParams(
            clientId = 1,
            responseType = VkAuth.ResponseType.AccessToken,
            redirectUri = VkAuth.VK_REDIRECT_URI_DEFAULT,
            scope = "offline",
        )
        val expectedUri = "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=${VkAuth.VK_API_VERSION_DEFAULT}&scope=offline&revoke=1"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    public fun `should build query without revoke`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            "offline",
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            false, "5.113"
        )
        val expectedUri =
            "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.113&scope=offline&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    public fun `should build query with list of scopes`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            listOf(VkAuth.Scope.Offline),
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            false, "5.113"
        )
        val expectedUri =
            "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.113&scope=65536&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    public fun `should build query with string scope`() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken,
            "offline",
            "https://oauth.vk.com/blank.html",
            VkAuth.Display.Mobile, "teststate1234",
            true, "5.113"
        )
        val expectedUri =
            "client_id=1&redirect_uri=https://oauth.vk.com/blank.html&response_type=token&display=mobile&v=5.113&scope=offline&revoke=1&state=teststate1234"

        assertEquals(expectedUri, params.asQuery())
    }

    @Test
    public fun shouldCorrectlyBuildBundle() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "offline", "https://oauth.vk.com/blank.html"
        )
        val bundle = params.asBundle(false)

        assertEquals(params.clientId, bundle.getInt("client_id"))
        assertEquals(params.redirectUri, bundle.getString("redirect_uri"))
        assertEquals(params.scope, bundle.getString("scope"))
        assertEquals(params.revoke, bundle.getBoolean("revoke"))
    }

    @Test
    public fun shouldCorrectlyBuildBundleWithIgnored() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "offline", "https://oauth.vk.com/blank.html"
        )
        val bundle = params.asBundle(true)

        assertEquals(params.clientId, bundle.getInt("client_id"))
        assertEquals(params.redirectUri, bundle.getString("redirect_uri"))
        assertEquals(params.scope, bundle.getString("scope"))
        assertEquals(params.revoke, bundle.getBoolean("revoke"))
        assertEquals(params.responseType.stringValue, bundle.getString("response_type"))
        assertEquals(params.display.stringValue, bundle.getString("display"))
        assertEquals(params.state, bundle.getString("state"))
    }
}