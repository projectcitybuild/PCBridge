package com.projectcitybuild.pcbridge.http.shared.logging

import okhttp3.Interceptor
import okhttp3.Response

class StructuredLoggingInterceptor(
    private val log: (Map<String, Any?>) -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }

        val start = System.currentTimeMillis()

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            log(
                mapOf(
                    "type" to "http_error",
                    "method" to request.method,
                    "url" to request.url.toString(),
                    "error" to e.message
                )
            )
            throw e
        }

        val duration = System.currentTimeMillis() - start
        val responseBodyString = response.peekBody(1024 * 1024) // 1MB

        log(
            mapOf(
                "type" to "http",
                "method" to request.method,
                "url" to request.url.toString(),
                "status" to response.code,
                "duration_ms" to duration,
                "request" to requestBody,
                "response" to responseBodyString.string(),
            )
        )
        return response
    }
}