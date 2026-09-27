package com.projectcitybuild.pcbridge.paper.features.maintenance.domain.data

import com.projectcitybuild.pcbridge.paper.runtime.state.store.featureStateKey

/**
 * The maintenance feature's own slice of persisted [Store] state.
 */
data class MaintenanceState(
    val enabled: Boolean = false,
)

val maintenanceStateKey = featureStateKey("maintenance", MaintenanceState())
