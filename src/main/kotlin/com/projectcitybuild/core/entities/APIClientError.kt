package com.projectcitybuild.core.entities

import com.projectcitybuild.core.entities.models.ApiError

sealed class APIClientError {
    class BAD_REQUEST: APIClientError()
    class EMPTY_RESPONSE: APIClientError()
    class MODEL_DESERIALIZE_FAILED: APIClientError()

    // An error returned in the API response body
    class BODY(val error: ApiError): APIClientError()
}