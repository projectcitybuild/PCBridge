package com.projectcitybuild.core.entities

import com.projectcitybuild.core.entities.models.ApiError

data class APIClientError(
    val body: ApiError?,
    val type: APIClientErrorType
) {
    val message: String
        get() = when(type) {
            APIClientErrorType.DESERIALIZE_FAILED -> "Model deserialization failed"
            APIClientErrorType.EMPTY_RESPONSE -> "No response body provided by the server"
            APIClientErrorType.BAD_REQUEST -> "Bad request format: ${body?.detail ?: "No further details"}"
            APIClientErrorType.RESPONSE_BODY -> body?.detail ?: "No further details"
        }

    val responseBody: ApiError?
        get() = if (type == APIClientErrorType.RESPONSE_BODY && body != null) body else null

    companion object {
        val emptyResponse: APIClientError
            get() = APIClientError(body = null, type = APIClientErrorType.EMPTY_RESPONSE)

        val deserializeFailed: APIClientError
            get() = APIClientError(body = null, type = APIClientErrorType.DESERIALIZE_FAILED)

        fun badRequest(error: ApiError): APIClientError {
            return APIClientError(body = error, type = APIClientErrorType.BAD_REQUEST)
        }
        fun responseBody(error: ApiError): APIClientError {
            return APIClientError(body = error, type = APIClientErrorType.RESPONSE_BODY)
        }
    }
}