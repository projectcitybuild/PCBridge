package com.projectcitybuild.pcbridge.paper.features.motd.decorators

import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListing
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecorator
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class GeneralMotdDecorator(
    private val remoteConfig: RemoteConfig,
) : ServerListingDecorator {
    override suspend fun decorate(prev: ServerListing): ServerListing {
        if (prev.motd != null) return prev

        val state = remoteConfig.latest.config
        val motd = state.motd

        return prev.copy(
            motd = if (motd.isNotEmpty()) {
                MiniMessage.miniMessage().deserialize(motd)
            } else {
                Component.text("Project City Build")
            }
        )
    }
}
