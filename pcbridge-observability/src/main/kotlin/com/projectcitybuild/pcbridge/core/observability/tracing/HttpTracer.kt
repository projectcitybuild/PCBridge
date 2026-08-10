package com.projectcitybuild.pcbridge.core.observability.tracing

import okhttp3.Call
import okhttp3.OkHttpClient

interface HttpTracer {
    fun instrument(client: OkHttpClient): Call.Factory
}