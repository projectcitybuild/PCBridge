package com.projectcitybuild.core.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.APIClientError
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.utilities.AsyncTask
import retrofit2.Call
import java.io.IOException

class APIClient(
        private val logger: LoggerProvider,
        private val scheduler: SchedulerProvider
) {
    sealed class Result<T> {
        data class SUCCESS<T>(val value: T) : Result<T>()
        data class FAILURE<T>(val error: APIClientError) : Result<T>()
    }

    fun <T> execute(request: Call<ApiResponse<T>>): AsyncTask<Result<T>> {
        return scheduler.async { resolver ->
            val response = request.execute()

            if (!response.isSuccessful) {
                val errorJson = response.errorBody()?.string()
                if (errorJson == null) {
                    logger.warning("API error response body was empty")
                    resolver(Result.FAILURE(APIClientError.EMPTY_RESPONSE()))
                    return@async
                }

                try {
                    val type = object : TypeToken<ApiResponse<GameBan>>() {}.type // Cannot do ApiResponse<T>::class.java
                    val errorModel: ApiResponse<T> = Gson().fromJson(errorJson, type)
                    if (errorModel.error == null) {
                        resolver(Result.FAILURE(APIClientError.MODEL_DESERIALIZE_FAILED()))
                        return@async
                    }
                    if (errorModel.error.id == "bad_input") {

                    }
                    resolver(Result.FAILURE(APIClientError.BODY(errorModel.error)))

                } catch (e: IOException) {
                    e.printStackTrace()
                    resolver(Result.FAILURE(APIClientError.MODEL_DESERIALIZE_FAILED()))
                }
                return@async
            }

            val data = response.body()?.data
            if (data == null) {
                logger.warning("API success response body was empty")
                resolver(Result.FAILURE(APIClientError.EMPTY_RESPONSE()))
            } else {
                resolver(Result.SUCCESS(data))
            }
        }
    }
}