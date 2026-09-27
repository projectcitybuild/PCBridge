package com.projectcitybuild.pcbridge.paper.runtime.connection

import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory

val connectionTracer = TracerFactory.make("connection")
