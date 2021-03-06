package com.petersamokhin.vksdk.android.auth

import android.app.Activity
import com.petersamokhin.vksdk.android.auth.activity.VkAuthActivity
import com.petersamokhin.vksdk.android.auth.error.VkAuthCanceledException
import com.petersamokhin.vksdk.android.auth.model.VkAuthResult
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.rules.ExpectedException

class VkResultParserTest {
    private val requestCode = 1337

    @Suppress("DEPRECATION")
    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun `should return null for incorrect result code`() {
        val actualResult = VkResultParser.parse(0, Activity.RESULT_CANCELED, mapOf())
        assertNull(actualResult)
    }

    @Test
    fun `should parse cancel result correctly`() {
        val expectedErrorText = "some_error_text"
        val expectedErrorReason = "some_error_reason"
        val expectedErrorDescription = "some_error_description"
        val uri = "https://oauth.vk.com/blank.html#error=$expectedErrorText&error_reason=$expectedErrorReason&error_description=$expectedErrorDescription"
        val expectedResult = VkAuthResult.Error(
            expectedErrorText,
            expectedErrorDescription,
            expectedErrorReason
        )

        val params = mapOf<String, Any?>(VkAuthActivity.EXTRA_AUTH_RESULT to uri)
        val actualResult = VkResultParser.parse(requestCode, Activity.RESULT_CANCELED, params)

        assertTrue(actualResult is VkAuthResult.Error)
        assertTrue((actualResult as VkAuthResult.Error).exception is VkAuthCanceledException)
        assertEquals(expectedResult.error, actualResult.error)
        assertEquals(expectedResult.reason, actualResult.reason)
        assertEquals(expectedResult.description, actualResult.description)
    }

    @Test
    fun `should parse error result correctly`() {
        val expectedErrorText = "some_error_text"
        val expectedErrorReason = "some_error_reason"
        val expectedErrorDescription = "some_error_description"
        val uri = "https://oauth.vk.com/blank.html#error=$expectedErrorText&error_reason=$expectedErrorReason&error_description=$expectedErrorDescription"
        val expectedResult = VkAuthResult.Error(
            expectedErrorText,
            expectedErrorDescription,
            expectedErrorReason
        )

        val params = mapOf<String, Any?>(
            VkAuthActivity.EXTRA_AUTH_RESULT to uri
        )

        assertEquals(expectedResult, VkResultParser.parse(requestCode, Activity.RESULT_OK, params))
    }

    @Test
    fun `should parse code result correctly`() {
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

        assertEquals(expectedResult, VkResultParser.parse(requestCode, Activity.RESULT_OK, params))
    }

    @Test
    fun `should parse access token result correctly`() {
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

        assertEquals(expectedResult, VkResultParser.parse(requestCode, Activity.RESULT_OK, params))
    }

    @Test
    fun `should throw an exception for the invalid format`() {
        val uri = "https://oauth.vk.com/blank.html?test"
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    fun `should throw an exception for the empty uri`() {
        val uri = ""
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    fun `should throw an exception for the wrong format with starts from sharp symbol`() {
        val uri = "#"
        expectedException.expect(IllegalArgumentException::class.java)
        VkResultParser.parseVkUri(uri)
    }

    @Test
    fun `should parse keys with the empty values`() {
        val uri = "https://oauth.vk.com/blank.html#test"
        val expectedMap = mapOf("test" to "")
        assertEquals(expectedMap, VkResultParser.parseVkUri(uri))
    }

    @Test
    fun `should parse empty query`() {
        val uri = "https://oauth.vk.com/blank.html#"
        val expectedMap = mapOf<String, String>()
        assertEquals(expectedMap, VkResultParser.parseVkUri(uri))
    }

    @Test
    fun `should parse valid query`() {
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
}