package com.projectcitybuild.pcbridge.http.shared.parsing

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ResponseParser {
    data class ValidationErrorBody(
        val message: String?,
        val errors: Map<String, List<String>>?
    )

    class NotFoundError : Exception()

    class ValidationError(message: String?) : Exception(message)

    suspend fun <T> parse(apiCall: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            try {
                apiCall.invoke()
            } catch (e: IOException) {
                throw e
            } catch (e: HttpException) {
                when (e.code()) {
                    404 -> throw NotFoundError()
                    422 -> {
                        val body = e.response()?.errorBody()?.string().let {
                            val errorBody = Gson().fromJson(it, ValidationErrorBody::class.java)
                            errorBody
                        }
                        throw ValidationError(body.message)
                    }
                    else -> throw e
                }
            } catch (e: Exception) {
                throw e
            }
        }
}
