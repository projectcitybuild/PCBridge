package com.projectcitybuild.core.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.APIClientError
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.entities.models.MojangPlayer
import com.projectcitybuild.core.utilities.AsyncTask
import retrofit2.Call
import java.io.IOException

class APIClient(
        private val logger: LoggerProvider,
        private val scheduler: SchedulerProvider
) {
    fun <T> execute(request: Call<ApiResponse<T>>): AsyncTask<Result<T, APIClientError>> {
        return scheduler.async { resolver ->
            val response = request.execute()

            if (!response.isSuccessful) {
                val errorJson = response.errorBody()?.string()
                if (errorJson == null) {
                    logger.warning("API error response body was empty")
                    resolver(Failure(APIClientError.emptyResponse))
                    return@async
                }
                try {
                    val type = object : TypeToken<ApiResponse<GameBan>>() {}.type // Can't do ApiResponse<T>::class.java
                    val errorModel: ApiResponse<T> = Gson().fromJson(errorJson, type)
                    if (errorModel.error == null) {
                        resolver(Failure(APIClientError.deserializeFailed))
                        return@async
                    }
                    if (errorModel.error.id == "bad_input") {
                        logger.warning("Bad request format: ${errorModel.error}")
                        resolver(Failure(APIClientError.badRequest(errorModel.error)))
                        return@async
                    }
                    resolver(Failure(APIClientError.responseBody(errorModel.error)))

                } catch (e: IOException) {
                    e.printStackTrace()
                    resolver(Failure(APIClientError.deserializeFailed))
                }
                return@async
            }

            val data = response.body()?.data
            if (data == null) {
                logger.warning("API success response body was empty")
                resolver(Failure(APIClientError.emptyResponse))
            } else {
                resolver(Success(data))
            }
        }
    }

    @Deprecated("Need a better way of doing this")
    fun executeMojang(request: Call<MojangPlayer>): AsyncTask<Result<MojangPlayer, APIClientError>> {
        return scheduler.async { resolver ->
            val response = request.execute()

            if (!response.isSuccessful) {
                val errorJson = response.errorBody()?.string()
                if (errorJson == null) {
                    logger.warning("API error response body was empty")
                    resolver(Failure(APIClientError.emptyResponse))
                    return@async
                }
                val error = ApiError(
                        id = "",
                        title = "",
                        detail = "",
                        status = response.code()
                )
                resolver(Failure(APIClientError.responseBody(error)))
                return@async
            }

            val data = response.body()
            if (data == null) {
                logger.warning("API success response body was empty")
                resolver(Failure(APIClientError.emptyResponse))
            } else {
                resolver(Success(data))
            }
        }
    }
}