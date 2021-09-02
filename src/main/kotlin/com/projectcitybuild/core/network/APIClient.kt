package com.projectcitybuild.core.network

import com.github.shynixn.mccoroutine.minecraftDispatcher
import com.google.gson.Gson
import com.projectcitybuild.core.contracts.LoggerProvider
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
        private val plugin: Plugin,
        private val logger: LoggerProvider
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
                logger.debug(throwable.toString())

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
}