package com.projectcitybuild.core.network

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.google.gson.Gson
import com.projectcitybuild.core.entities.models.ApiError
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

sealed class APIResult<out T> {
    data class Success<out T>(val value: T): APIResult<T>()
    data class HTTPError(val code: Int? = null, val error: ApiError? = null): APIResult<Nothing>()
    object NetworkError: APIResult<Nothing>()
}

class APIClient(
    private val getCoroutineContext: () -> CoroutineContext
) {
    data class ErrorBody(val error: ApiError)

    suspend fun <T> execute(apiCall: suspend () -> T): APIResult<T> {
        return withContext(getCoroutineContext()) {
            try {
                APIResult.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> APIResult.NetworkError
                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = convertErrorBody(throwable)
                        APIResult.HTTPError(code, errorResponse)
                    }
                    else -> {
                        APIResult.HTTPError(null, null)
                    }
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): ApiError? {
        throwable.response()?.errorBody()?.string().let {
            return Gson().fromJson(it, ErrorBody::class.java).error
        }
    }
}