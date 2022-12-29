package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAuthCanceledException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
public class VkResultParserTest {
    @Suppress("DEPRECATION")
    @get:Rule
    public val expectedException: ExpectedException = ExpectedException.none()

    @Test
    public fun `should parse cancel result correctly`() {
        val expectedErrorText = "some_error_text"
        val expectedErrorReason = "some_error_reason"
        val expectedErrorDescription = "some_error_description"
        val uri =
            "https://oauth.vk.com/blank.html#error=$expectedErrorText&error_reason=$expectedErrorReason&error_description=$expectedErrorDescription"
        val expectedResult = VkAuthResult.Error(
            expectedErrorText,
            expectedErrorDescription,
            expectedErrorReason
        )

        val params = mapOf<String, Any?>(VkAuthActivity.EXTRA_AUTH_RESULT to uri)
        val actualResult = VkResultParser.parse(Activity.RESULT_CANCELED, params)

        assertTrue(actualResult is VkAuthResult.Error)
        assertTrue((actualResult as VkAuthResult.Error).exception is VkAuthCanceledException)
        assertEquals(expectedResult.error, actualResult.error)
        assertEquals(expectedResult.reason, actualResult.reason)
        assertEquals(expectedResult.description, actualResult.description)
    }

    @Test
    public fun `should parse error result correctly`() {
        val expectedErrorText = "some_error_text"
        val expectedErrorReason = "some_error_reason"
        val expectedErrorDescription = "some_error_description"
        val uri =
            "https://oauth.vk.com/blank.html#error=$expectedErrorText&error_reason=$expectedErrorReason&error_description=$expectedErrorDescription"
        val expectedResult = VkAuthResult.Error(
            expectedErrorText,
            expectedErrorDescription,
            expectedErrorReason
        )

        val params = mapOf<String, Any?>(
            VkAuthActivity.EXTRA_AUTH_RESULT to uri
        )

        assertEquals(expectedResult, VkResultParser.parse(Activity.RESULT_OK, params))
    }

    @Test
    public fun `should parse code result correctly`() {
        val expectedCode = "code1234"
        val expectedState = "state1234"
        val uri = "https://oauth.vk.com/blank.html#code=$expectedCode&state=$expectedState"
        val expectedResult = VkAuthResult.Code(
            expectedCode,
            expectedState
        )

        val params = mapOf<String, Any?>(
            VkAuthActivity.EXTRA_AUTH_RESULT to uri
        )

        assertEquals(expectedResult, VkResultParser.parse(Activity.RESULT_OK, params))
    }

    @Test
    public fun `should parse access token result correctly`() {
        val expectedToken = "token1234"
        val expectedState = "state1234"
        val expectedEmail = "test@example.com"
        val expectedExpiresIn = 0
        val expectedUserId = 1
        val uri = "https://oauth.vk.com/blank.html#access_token=$expectedToken&state=$expectedState&email=$expectedEmail&expires_in=$expectedExpiresIn&user_id=$expectedUserId"
        val expectedResult = VkAuthResult.AccessToken(
            expectedToken,
            expectedExpiresIn,
            expectedUserId,
            expectedEmail,
            expectedState
        )

        val params = mapOf<String, Any?>(
            VkAuthActivity.EXTRA_AUTH_RESULT to uri
        )

        assertEquals(expectedResult, VkResultParser.parse(Activity.RESULT_OK, params))
    }

    @Test
    public fun `should throw an exception for the invalid format`() {
        val uri = "https://oauth.vk.com/blank.html?test"
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    public fun `should throw an exception for the empty uri`() {
        val uri = ""
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    public fun `should throw an exception for the wrong format with starts from sharp symbol`() {
        val uri = "#"
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    public fun `should parse keys with the empty values`() {
        val uri = "https://oauth.vk.com/blank.html#test"
        val expectedMap = mapOf("test" to "")
        assertEquals(expectedMap, VkResultParser.parseVkUri(uri))
    }

    @Test
    public fun `should parse empty query`() {
        val uri = "https://oauth.vk.com/blank.html#"
        val expectedMap = mapOf<String, String>()
        assertEquals(expectedMap, VkResultParser.parseVkUri(uri))
    }

    @Test
    public fun `should parse valid query`() {
        val expectedCode = "code1234"
        val expectedState = "state1234"
        val expectedEmail = "test@example.com"
        val uri = "https://oauth.vk.com/blank.html#code=$expectedCode&state=$expectedState&email=$expectedEmail"
        val expectedMap = mapOf(
            "code" to expectedCode,
            "state" to expectedState,
            "email" to expectedEmail
        )
        assertEquals(expectedMap, VkResultParser.parseVkUri(uri))
    }

    @Test
    public fun `should parse valid custom tabs result`() {
        val expectedToken = "token1234"
        val expectedState = "state1234"
        val expectedEmail = "test@example.com"
        val expectedExpiresIn = 0
        val expectedUserId = 1
        val uri = "https://oauth.vk.com/blank.html#access_token=$expectedToken&state=$expectedState&email=$expectedEmail&expires_in=$expectedExpiresIn&user_id=$expectedUserId"
        val twiceEncodedUri = Uri.encode(Uri.encode(uri))

        val referrer = "https://oauth.vk.com/auth_redirect?app_id=12345&authorize_url=$twiceEncodedUri&redirect_hash=123456"

        val expectedResult = VkAuthResult.AccessToken(
            expectedToken,
            expectedExpiresIn,
            expectedUserId,
            expectedEmail,
            expectedState
        )

        assertEquals(expectedResult, VkResultParser.parseCustomTabs(mapOf(Intent.EXTRA_REFERRER to referrer)))
    }
}