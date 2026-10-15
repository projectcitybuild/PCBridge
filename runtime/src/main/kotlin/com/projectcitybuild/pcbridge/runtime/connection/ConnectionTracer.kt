package com.projectcitybuild.pcbridge.runtime.connection

import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory

val connectionTracer = TracerFactory.make("connection")
