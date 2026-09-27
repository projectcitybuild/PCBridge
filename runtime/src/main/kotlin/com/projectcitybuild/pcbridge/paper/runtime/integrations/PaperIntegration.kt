package com.projectcitybuild.pcbridge.paper.runtime.integrations

interface PaperIntegration {
    val priority: Int

    suspend fun enable()

    fun disable()
}

class PaperIntegrationRegistrar {
    suspend fun enable(integrations: List<PaperIntegration>) {
        integrations
            .sortedBy { it.priority }
            .forEach { it.enable() }
    }

    fun disable(integrations: List<PaperIntegration>) {
        integrations
            .sortedBy { it.priority }
            .forEach { it.disable() }
    }
}
