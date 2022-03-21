package com.projectcitybuild.core.infrastructure.network

import com.projectcitybuild.entities.responses.ApiError

interface APIClient {
    data class ErrorBody(val error: ApiError)

    class HTTPError(val errorBody: ApiError?) : Exception(
        if (errorBody != null) "Bad response received from the ban server: ${errorBody.detail}"
        else "Bad response received from the ban server (no error given)"
    )

    class NetworkError : Exception(
        "Failed to contact PCB auth server"
    )

    suspend fun <T> execute(apiCall: suspend () -> T): T
}
