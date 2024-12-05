package com.projectcitybuild.pcbridge.webserver.data

data class HttpServerConfig(
    val authToken: String,
    val port: Int,
)