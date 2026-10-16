package com.projectcitybuild.pcbridge.core.http.parsing

import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ResponseParserTest {
    private val parser = ResponseParser()

    @Test
    fun `parse should return successful results`() = runBlocking {
        assertEquals("result", parser.parse { "result" })
    }

    @Test
    fun `parse should map known http errors and include response messages`() {
        val notFound = parseHttpError(404)
        assertTrue(notFound is ResponseParserError.NotFound)
        assertEquals("request rejected", notFound.message)

        val forbidden = parseHttpError(403)
        assertTrue(forbidden is ResponseParserError.Forbidden)

        val conflict = parseHttpError(409)
        assertTrue(conflict is ResponseParserError.Conflict)
        assertEquals("request rejected", conflict.message)

        val validation = parseHttpError(422)
        assertTrue(validation is ResponseParserError.Validation)
        assertEquals("request rejected", validation.message)
    }

    @Test
    fun `parse should preserve unrecognized http and io errors`() {
        val httpError = HttpException(
            Response.error<String>(
                500,
                """{"message":"server error"}""".toResponseBody(),
            ),
        )
        val thrownHttpError = assertThrows(HttpException::class.java) {
            runBlocking {
                parser.parse<String> { throw httpError }
            }
        }
        assertSame(httpError, thrownHttpError)

        val ioError = IOException("network unavailable")
        val thrownIoError = assertThrows(IOException::class.java) {
            runBlocking {
                parser.parse<String> { throw ioError }
            }
        }
        assertEquals("network unavailable", thrownIoError.message)
    }

    private fun parseHttpError(code: Int): ResponseParserError =
        assertThrows(ResponseParserError::class.java) {
            runBlocking {
                parser.parse<String> {
                    throw HttpException(
                        Response.error<String>(
                            code,
                            """{"message":"request rejected","errors":{"name":["invalid"]}}""".toResponseBody(),
                        ),
                    )
                }
            }
        }
}
