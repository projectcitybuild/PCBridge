package com.projectcitybuild.pcbridge.paper.features.stats

import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory

val statsTracer = TracerFactory.make("features.stats")