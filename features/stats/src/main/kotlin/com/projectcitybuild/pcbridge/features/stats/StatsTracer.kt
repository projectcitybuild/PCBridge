package com.projectcitybuild.pcbridge.features.stats

import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory

val statsTracer = TracerFactory.make("features.stats")