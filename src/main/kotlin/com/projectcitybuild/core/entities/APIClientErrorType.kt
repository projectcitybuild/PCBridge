package com.projectcitybuild.core.entities

enum class APIClientErrorType {
    BAD_REQUEST,
    EMPTY_RESPONSE,
    DESERIALIZE_FAILED,
    RESPONSE_BODY, // An error returned in the API response body
}