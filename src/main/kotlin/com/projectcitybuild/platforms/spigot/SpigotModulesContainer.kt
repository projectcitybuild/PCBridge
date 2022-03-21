package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.teleporthistory.TeleportHistoryModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.integrations.plugins.dynmap.DynmapIntegrationModule
import com.projectcitybuild.integrations.shared.crossteleport.CrossTeleportModule
import javax.inject.Inject

class SpigotModulesContainer @Inject constructor(
    chatModule: ChatModule.Spigot,
    crossTeleportModule: CrossTeleportModule,
    dynmapIntegrationModule: DynmapIntegrationModule,
    hubModule: HubModule,
    joinMessageModule: JoinMessageModule.Spigot,
    teleportModule: TeleportModule.Spigot,
    teleportHistoryModule: TeleportHistoryModule.Spigot,
    warpModule: WarpModule,
) {
    val modules: List<SpigotFeatureModule> = listOf(
        chatModule,
        crossTeleportModule,
        dynmapIntegrationModule,
        hubModule,
        joinMessageModule,
        teleportModule,
        teleportHistoryModule,
        warpModule,
    )
}
