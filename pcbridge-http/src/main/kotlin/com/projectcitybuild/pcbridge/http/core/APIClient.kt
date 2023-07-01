package com.projectcitybuild.pcbridge.http.core

import com.projectcitybuild.pcbridge.http.responses.ApiError

interface APIClient {
    data class ErrorBody(val error: ApiError)

    class HTTPError(val errorBody: ApiError?) : Exception(
        if (errorBody != null) "Bad response received from the server: ${errorBody.detail}"
        else "Bad response received from the server (no error given)"
    )

    class NetworkError : Exception(
        "Failed to contact PCB auth server"
    )

    suspend fun <T> execute(apiCall: suspend () -> T): T
}
