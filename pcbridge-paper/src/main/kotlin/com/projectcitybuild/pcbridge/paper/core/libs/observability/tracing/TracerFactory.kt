package com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing

class TracerFactory private constructor() {
    companion object {
        private var instance: OpenTelemetryProvider? = null

        fun configure(otel: OpenTelemetryProvider) {
            instance = otel
        }

        fun make(module: String): Tracer {
            return makeWithNamespace("com.projectcitybuild.pcbridge.$module")
        }

        fun makeWithNamespace(namespace: String): Tracer {
            if (instance == null) {
                throw Exception("TracerFactory instance missing - did you forget to call configure()?")
            }
            return Tracer(name = namespace, otel = instance!!)
        }
    }
}