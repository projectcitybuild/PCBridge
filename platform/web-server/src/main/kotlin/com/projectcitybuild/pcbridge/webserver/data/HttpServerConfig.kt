package com.projectcitybuild.pcbridge.webserver.data

data class HttpServerConfig(
    val host: String,
    val port: Int,
    val authToken: String,
)