package com.projectcitybuild.pcbridge.http.shared.logging

import okhttp3.logging.HttpLoggingInterceptor

class HttpLogger(
    private val handler: (String) -> Unit,
): HttpLoggingInterceptor.Logger {
    override fun log(message: String) = handler(message)
}