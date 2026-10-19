package com.projectcitybuild.pcbridge.features.maintenance.hooks.decorators

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.runtime.serverlist.decorators.ServerListing
import com.projectcitybuild.pcbridge.runtime.serverlist.decorators.ServerListingDecorator
import com.projectcitybuild.pcbridge.runtime.state.store.Store

class MaintenanceMotdDecorator(
    private val store: Store,
) : ServerListingDecorator {
    override suspend fun decorate(prev: ServerListing): ServerListing {
        val state = store.state(maintenanceStateKey)
        if (!state.enabled) {
            return prev
        }
        return prev.copy(
            motd = l10n.maintenance.maintenanceMotd,
        )
    }
}
