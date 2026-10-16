package com.projectcitybuild.pcbridge.features.maintenance.domain.data

import com.projectcitybuild.pcbridge.runtime.state.store.featureStateKey

/**
 * The maintenance feature's own slice of persisted [Store] state.
 */
data class MaintenanceState(
    val enabled: Boolean = false,
)

val maintenanceStateKey = featureStateKey("maintenance", MaintenanceState())
