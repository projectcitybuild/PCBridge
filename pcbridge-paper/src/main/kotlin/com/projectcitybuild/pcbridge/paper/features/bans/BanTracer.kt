package com.projectcitybuild.pcbridge.paper.features.bans

import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory

val banTracer = TracerFactory.make(module = "bans")