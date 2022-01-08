package com.projectcitybuild.core.network

import com.google.gson.Gson
import com.projectcitybuild.entities.responses.ApiError
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class APIClient(
    private val getCoroutineContext: () -> CoroutineContext
) {
    data class ErrorBody(val error: ApiError)

    class HTTPError(val errorBody: ApiError?) : Exception(
        if (errorBody != null) "Bad response received from the ban server: ${errorBody.detail}"
        else "Bad response received from the ban server (no error given)"
    )

    class NetworkError : Exception(
        "Failed to contact PCB auth server"
    )

    suspend fun <T> execute(apiCall: suspend () -> T): T {
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