package com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.decorators

import com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators.ServerListing
import com.projectcitybuild.pcbridge.paper.runtime.serverlist.decorators.ServerListingDecorator
import com.projectcitybuild.pcbridge.paper.runtime.state.store.Store
import net.kyori.adventure.text.minimessage.MiniMessage

class MaintenanceMotdDecorator(
    private val store: Store,
) : ServerListingDecorator {
    override suspend fun decorate(prev: ServerListing): ServerListing {
        val state = store.state
        if (!state.maintenance) {
            return prev
        }
        return prev.copy(
            motd =
                MiniMessage.miniMessage().deserialize(
                    "<red>Server maintenance - be right back!</red>",
                ),
        )
    }
}
