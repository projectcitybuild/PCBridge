package com.projectcitybuild.pcbridge.paper.features.watchdog

import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory

val watchDogTracer = TracerFactory.make("features.watchdog")