package com.projectcitybuild.pcbridge.core.architecture.events

interface EventMiddleware<T> {
    suspend fun process(event: T): MiddlewareResult
}

enum class MiddlewareResult {
    allow,
    reject,
}