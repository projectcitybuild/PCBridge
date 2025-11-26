package com.projectcitybuild.pcbridge.http.shared.parsing

sealed class ResponseParserError(message: String? = null): Exception(message) {
    class NotFound(message: String?) : ResponseParserError(message)

    class Forbidden : ResponseParserError()

    class Conflict(message: String?) : ResponseParserError(message)

    class Validation(message: String?) : ResponseParserError(message)
}