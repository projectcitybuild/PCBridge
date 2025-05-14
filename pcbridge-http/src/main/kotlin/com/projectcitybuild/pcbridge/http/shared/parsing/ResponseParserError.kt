package com.projectcitybuild.pcbridge.http.shared.parsing

sealed class ResponseParserError(message: String? = null): Exception(message) {
    class NotFound : ResponseParserError()

    class Forbidden : ResponseParserError()

    class Validation(message: String?) : ResponseParserError(message)
}