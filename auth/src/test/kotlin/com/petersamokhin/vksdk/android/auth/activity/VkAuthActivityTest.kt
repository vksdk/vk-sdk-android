package com.petersamokhin.vksdk.android.auth.activity

import com.petersamokhin.vksdk.android.auth.VkAuth
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
public class VkAuthActivityTest {
    @Test
    public fun intent() {
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "offline", "https://oauth.vk.com/blank.html"
        )
        val intent = VkAuthActivity.intent(Robolectric.buildActivity(android.app.Activity::class.java).get(), params)

        assertEquals(params.asQuery(), intent.getStringExtra(VkAuthActivity.EXTRA_AUTH_QUERY))
        assertEquals(params.redirectUri, intent.getStringExtra(VkAuthActivity.EXTRA_AUTH_REDIRECT_URI))
    }
}