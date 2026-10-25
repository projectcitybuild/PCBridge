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
            disableActive().forEach(failure::addSuppressed)
            throw failure
        }
    }

    fun disable() {
        val failures = disableActive()
        val firstFailure = failures.firstOrNull() ?: return
        failures.drop(1).forEach(firstFailure::addSuppressed)
        throw firstFailure
    }

    fun disable(integrations: List<PaperIntegration>) {
        if (active.isEmpty()) {
            active.addAll(integrations)
        }
        disable()
    }

    private fun disableActive(): List<Throwable> {
        val failures = mutableListOf<Throwable>()
        active.asReversed().forEach { integration ->
            try {
                integration.disable()
            } catch (cleanupFailure: Throwable) {
                failures.add(cleanupFailure)
            }
        }
        active.clear()
        return failures
    }
}
