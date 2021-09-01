package com.projectcitybuild.core.network

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.google.gson.Gson
import com.projectcitybuild.core.entities.models.ApiError
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

sealed class APIResult<out T> {
    data class Success<out T>(val value: T): APIResult<T>()
    data class HTTPError(val code: Int? = null, val error: ApiError? = null): APIResult<Nothing>()
    object NetworkError: APIResult<Nothing>()
}

class APIClient(
        private val plugin: Plugin
) {
    private fun getCoroutineContext(): CoroutineContext {
        // Fetch instead of injecting, to prevent Coroutines being created
        // before the plugin is ready
        return plugin.minecraftDispatcher
    }

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
        return try {
            throwable.response()?.errorBody()?.string()?.let {
                return Gson().fromJson(it, ApiError::class.java)
            }
        } catch (exception: Exception) {
            null
        }
    }

//    suspend fun <T> executeSynchronously(request: ApiResponse<T>): Result<T?, APIClientError> {
//        val response = request
//
//        if (!response.isSuccessful) {
//            val errorJson = response.errorBody()?.string()
//            if (errorJson == null) {
//                logger.warning("API error response body was empty")
//                return Failure(APIClientError.emptyResponse)
//            }
//            try {
//                val type = object : TypeToken<ApiResponse<GameBan>>() {}.type // Can't do ApiResponse<T>::class.java
//                val errorModel: ApiResponse<T> = Gson().fromJson(errorJson, type)
//                if (errorModel.error == null) {
//                    return Failure(APIClientError.deserializeFailed)
//                }
//                if (errorModel.error.id == "bad_input") {
//                    logger.warning("Bad request format: ${errorModel.error}")
//                    return Failure(APIClientError.badRequest(errorModel.error))
//                }
//                return Failure(APIClientError.responseBody(errorModel.error))
//
//            } catch (e: IOException) {
//                e.printStackTrace()
//                return Failure(APIClientError.deserializeFailed)
//            }
//        }
//
//        val data = response.body()?.data
//        return Success(data)
//    }
}