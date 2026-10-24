package com.projectcitybuild.pcbridge.runtime.integrations

interface PaperIntegration {
    val priority: Int

    /**
     * Activates the integration. Implementations must undo any partial work before throwing.
     */
    suspend fun enable()

    /** Deactivates the integration; safe when enablement was partial or never completed. */
    fun disable()
}

class PaperIntegrationRegistrar {
    private val active = mutableListOf<PaperIntegration>()

    suspend fun enable(integrations: List<PaperIntegration>) {
        try {
            integrations.sortedBy { it.priority }.forEach { integration ->
                integration.enable()
                active.add(integration)
            }
        } catch (failure: Throwable) {
            disableActive(failure)
            throw failure
        }
    }

    fun disable() {
        var failure: Throwable? = null
        active.asReversed().forEach { integration ->
            try {
                integration.disable()
            } catch (cleanupFailure: Throwable) {
                if (failure == null) failure = cleanupFailure else failure!!.addSuppressed(cleanupFailure)
            }
        }
        active.clear()
        failure?.let { throw it }
    }

    fun disable(integrations: List<PaperIntegration>) {
        if (active.isEmpty()) {
            active.addAll(integrations)
        }
        disable()
    }

    private fun disableActive(originalFailure: Throwable) {
        active.asReversed().forEach { integration ->
            try {
                integration.disable()
            } catch (cleanupFailure: Throwable) {
                originalFailure.addSuppressed(cleanupFailure)
            }
        }
        active.clear()
    }
}
