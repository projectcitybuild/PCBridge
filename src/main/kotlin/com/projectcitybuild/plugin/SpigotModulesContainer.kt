package com.projectcitybuild.plugin

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.bans.BanModule
import com.projectcitybuild.features.chat.ChatModule
import com.projectcitybuild.features.hub.HubModule
import com.projectcitybuild.features.joinmessage.JoinMessageModule
import com.projectcitybuild.features.ranksync.RankSyncModule
import com.projectcitybuild.features.teleporthistory.TeleportHistoryModule
import com.projectcitybuild.features.teleporting.TeleportModule
import com.projectcitybuild.features.utility.UtilityModule
import com.projectcitybuild.features.warps.WarpModule
import com.projectcitybuild.integrations.dynmap.DynmapIntegrationModule
import com.projectcitybuild.integrations.gadgetsmenu.GadgetsMenuIntegrationModule
import com.projectcitybuild.shared.playercache.PlayerCacheModule
import javax.inject.Inject

class SpigotModulesContainer @Inject constructor(
    banModule: BanModule,
    chatModule: ChatModule,
    dynmapIntegrationModule: DynmapIntegrationModule,
    gadgetsMenuIntegrationModule: GadgetsMenuIntegrationModule,
    hubModule: HubModule,
    joinMessageModule: JoinMessageModule,
    playerCacheModule: PlayerCacheModule,
    rankSyncModule: RankSyncModule,
    teleportModule: TeleportModule,
    teleportHistoryModule: TeleportHistoryModule,
    utilityModule: UtilityModule,
    warpModule: WarpModule,
) {
    val modules: List<SpigotFeatureModule> = listOf(
        banModule,
        chatModule,
        dynmapIntegrationModule,
        gadgetsMenuIntegrationModule,
        hubModule,
        joinMessageModule,
        playerCacheModule,
        rankSyncModule,
        teleportModule,
        teleportHistoryModule,
        utilityModule,
        warpModule,
    )
}
