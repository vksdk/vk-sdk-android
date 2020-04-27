package com.petersamokhin.vksdk.android.auth

import org.junit.Assert.*
import org.junit.Test

class VkAuthParamsInstrumentationTest {
    @Test
    fun shouldCorrectlyBuildBundle() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "https://oauth.vk.com/blank.html", "offline"
        )
        val bundle = params.asBundle(false)

        assertEquals(params.clientId, bundle.getInt("client_id"))
        assertEquals(params.redirectUri, bundle.getString("redirect_uri"))
        assertEquals(params.scope, bundle.getString("scope"))
        assertEquals(params.revoke, bundle.getBoolean("revoke"))
    }

    @Test
    fun shouldCorrectlyBuildBundleWithIgnored() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "https://oauth.vk.com/blank.html", "offline"
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