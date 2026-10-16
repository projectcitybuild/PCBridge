package com.projectcitybuild.pcbridge.core.http.logging

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class StructuredLoggingInterceptorTest {
    @Test
    fun `intercept should log request and response while preserving response body`() {
        val logged = mutableListOf<Map<String, Any?>>()
        val interceptor = StructuredLoggingInterceptor(logged::add)
        val request = Request.Builder()
            .url("https://api.example.test/items")
            .post("request body".toRequestBody("text/plain".toMediaType()))
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(201)
            .message("Created")
            .body("response body".toResponseBody())
            .build()

        val chain = mock<Interceptor.Chain>()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertSame(response, result)
        assertEquals("response body", result.body?.string())
        assertEquals(1, logged.size)
        assertEquals("http", logged.single()["type"])
        assertEquals("POST", logged.single()["method"])
        assertEquals(request.url.toString(), logged.single()["url"])
        assertEquals(201, logged.single()["status"])
        assertEquals("request body", logged.single()["request"])
        assertEquals("response body", logged.single()["response"])
    }

    @Test
    fun `intercept should log and rethrow request failures`() {
        val logged = mutableListOf<Map<String, Any?>>()
        val interceptor = StructuredLoggingInterceptor(logged::add)
        val request = Request.Builder().url("https://api.example.test/items").build()
        val failure = IOException("connection failed")

        val chain = mock<Interceptor.Chain>()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenThrow(failure)

        val thrown = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        assertSame(failure, thrown)
        assertEquals("http_error", logged.single()["type"])
        assertEquals("connection failed", logged.single()["error"])
    }
}
