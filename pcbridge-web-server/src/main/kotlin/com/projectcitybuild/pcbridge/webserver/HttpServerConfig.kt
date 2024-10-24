package com.projectcitybuild.pcbridge.webserver

data class HttpServerConfig(
    val authToken: String,
    val port: Int,
)