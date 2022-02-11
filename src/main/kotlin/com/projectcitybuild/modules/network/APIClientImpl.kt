package com.projectcitybuild.modules.network

import com.google.gson.Gson
import com.projectcitybuild.entities.responses.ApiError
import dagger.Reusable
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

@Reusable
class APIClientImpl(
    private val getCoroutineContext: () -> CoroutineContext
): APIClient {

    override suspend fun <T> execute(apiCall: suspend () -> T): T {
       return withContext(getCoroutineContext()) {
            try {
                apiCall.invoke()
            } catch (_: IOException) {
                throw APIClient.NetworkError()
            } catch (e: HttpException) {
                val code = e.code()
                throw APIClient.HTTPError(errorBody = convertErrorBody(e, code))
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun convertErrorBody(e: HttpException, code: Int): ApiError? {
        e.response()?.errorBody()?.string().let {
            val errorBody = Gson().fromJson(it, APIClient.ErrorBody::class.java).error
            errorBody.status = code
            return errorBody
        }
    }
}