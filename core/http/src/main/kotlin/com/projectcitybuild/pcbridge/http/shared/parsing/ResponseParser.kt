package com.projectcitybuild.pcbridge.http.shared.parsing

import com.google.gson.Gson
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser.ValidationErrorBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ResponseParser {
    data class ValidationErrorBody(
        val message: String?,
        val errors: Map<String, List<String>>?
    )

    suspend fun <T> parse(apiCall: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            try {
                apiCall.invoke()
            } catch (e: IOException) {
                throw e
            } catch (e: HttpException) {
                val body = e.response()?.getErrorBody()
                throw when (e.code()) {
                    409 -> ResponseParserError.Conflict(body?.message)
                    403 -> ResponseParserError.Forbidden()
                    404 -> ResponseParserError.NotFound(body?.message)
                    422 -> ResponseParserError.Validation(body?.message)
                    else -> e
                }
            } catch (e: Exception) {
                throw e
            }
        }
}

private fun Response<*>.getErrorBody(): ValidationErrorBody? {
    return errorBody()?.string().let {
        val errorBody = Gson().fromJson(it, ValidationErrorBody::class.java)
        errorBody
    }
}