package com.projectcitybuild.pcbridge.paper.features.watchdog

import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory

val watchDogTracer = TracerFactory.make("features.watchdog")