package com.petersamokhin.vksdk.android.auth.activity

import android.app.Activity
import com.petersamokhin.vksdk.android.auth.VkAuth
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.mock

class VkAuthActivityTest {
    @Test
    fun intent() {
        val activity = mock(Activity::class.java)
        val params = VkAuth.AuthParams(
            1, VkAuth.ResponseType.AccessToken, "offline", "https://oauth.vk.com/blank.html"
        )
        val intent = VkAuthActivity.intent(activity, params)

        assertEquals(params.asQuery(), intent.getStringExtra(VkAuthActivity.EXTRA_AUTH_QUERY))
        assertEquals(params.redirectUri, intent.getStringExtra(VkAuthActivity.EXTRA_AUTH_REDIRECT_URI))
    }
}