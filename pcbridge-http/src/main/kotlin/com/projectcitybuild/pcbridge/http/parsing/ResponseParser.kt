package com.projectcitybuild.pcbridge.http.parsing

import com.google.gson.Gson
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class ResponseParser(
    private val getCoroutineContext: () -> CoroutineContext
) {
    data class ErrorBody(val error: ApiError)

    class HTTPError(val errorBody: ApiError?) : Exception(
        if (errorBody != null) "Bad response received from the server: ${errorBody.detail}"
        else "Bad response received from the server (no error given)"
    )

    class NetworkError : Exception(
        "Failed to contact PCB auth server"
    )

    suspend fun <T> parse(apiCall: suspend () -> T): T {
        return withContext(getCoroutineContext()) {
            try {
                apiCall.invoke()
            } catch (_: IOException) {
                throw NetworkError()
            } catch (e: HttpException) {
                val code = e.code()
                throw HTTPError(errorBody = convertErrorBody(e, code))
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun convertErrorBody(e: HttpException, code: Int): ApiError? {
        e.response()?.errorBody()?.string().let {
            val errorBody = Gson().fromJson(it, ErrorBody::class.java).error
            errorBody.status = code
            return errorBody
        }
    }
}
