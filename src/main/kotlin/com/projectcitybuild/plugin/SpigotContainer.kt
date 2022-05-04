package com.projectcitybuild.plugin

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.integrations.dynmap.DynmapIntegrationModule
import com.projectcitybuild.integrations.gadgetsmenu.GadgetsMenuIntegrationModule
import com.projectcitybuild.plugin.commands.ACommand
import com.projectcitybuild.plugin.commands.BackCommand
import com.projectcitybuild.plugin.commands.BanCommand
import com.projectcitybuild.plugin.commands.BanIPCommand
import com.projectcitybuild.plugin.commands.CheckBanCommand
import com.projectcitybuild.plugin.commands.DelWarpCommand
import com.projectcitybuild.plugin.commands.HubCommand
import com.projectcitybuild.plugin.commands.IgnoreCommand
import com.projectcitybuild.plugin.commands.MuteCommand
import com.projectcitybuild.plugin.commands.PCBridgeCommand
import com.projectcitybuild.plugin.commands.ReplyCommand
import com.projectcitybuild.plugin.commands.SetHubCommand
import com.projectcitybuild.plugin.commands.SetWarpCommand
import com.projectcitybuild.plugin.commands.SyncCommand
import com.projectcitybuild.plugin.commands.SyncOtherCommand
import com.projectcitybuild.plugin.commands.TPACommand
import com.projectcitybuild.plugin.commands.TPAHereCommand
import com.projectcitybuild.plugin.commands.TPAcceptCommand
import com.projectcitybuild.plugin.commands.TPCommand
import com.projectcitybuild.plugin.commands.TPDenyCommand
import com.projectcitybuild.plugin.commands.TPHereCommand
import com.projectcitybuild.plugin.commands.TPOCommand
import com.projectcitybuild.plugin.commands.TPOHereCommand
import com.projectcitybuild.plugin.commands.TPToggleCommand
import com.projectcitybuild.plugin.commands.UnbanCommand
import com.projectcitybuild.plugin.commands.UnbanIPCommand
import com.projectcitybuild.plugin.commands.UnignoreCommand
import com.projectcitybuild.plugin.commands.UnmuteCommand
import com.projectcitybuild.plugin.commands.WarpCommand
import com.projectcitybuild.plugin.commands.WarpsCommand
import com.projectcitybuild.plugin.commands.WhisperCommand
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.listeners.BanConnectionListener
import com.projectcitybuild.plugin.listeners.ChatListener
import com.projectcitybuild.plugin.listeners.FirstTimeJoinMessageListener
import com.projectcitybuild.plugin.listeners.PlayerPreTeleportListener
import com.projectcitybuild.plugin.listeners.ServerJoinMessageListener
import com.projectcitybuild.plugin.listeners.SyncRankLoginListener
import com.projectcitybuild.plugin.listeners.WelcomeMessageListener
import com.projectcitybuild.shared.playercache.PlayerCacheModule
import javax.inject.Inject

class SpigotContainer @Inject constructor(
    val modules: Modules,
    val commands: Commands,
    val listeners: Listeners,
    val integrations: Integrations,
) {
    class Modules @Inject constructor(
        dynmapIntegrationModule: DynmapIntegrationModule,
        gadgetsMenuIntegrationModule: GadgetsMenuIntegrationModule,
        playerCacheModule: PlayerCacheModule,
    ) {
        val modules: List<SpigotFeatureModule> = listOf(
            dynmapIntegrationModule,
            gadgetsMenuIntegrationModule,
            playerCacheModule,
        )
    }

    class Commands @Inject constructor(
        banCommand: BanCommand,
        banIPCommand: BanIPCommand,
        unbanCommand: UnbanCommand,
        unbanIPCommand: UnbanIPCommand,
        checkBanCommand: CheckBanCommand,
        hubCommand: HubCommand,
        setHubCommand: SetHubCommand,
        aCommand: ACommand,
        ignoreCommand: IgnoreCommand,
        muteCommand: MuteCommand,
        replyCommand: ReplyCommand,
        unignoreCommand: UnignoreCommand,
        unmuteCommand: UnmuteCommand,
        whisperCommand: WhisperCommand,
        syncCommand: SyncCommand,
        syncOtherCommand: SyncOtherCommand,
        backCommand: BackCommand,
        tpCommand: TPCommand,
        tpHereCommand: TPHereCommand,
        tpaCommand: TPACommand,
        tpaHereCommand: TPAHereCommand,
        tpAcceptCommand: TPAcceptCommand,
        tpDenyCommand: TPDenyCommand,
        tpoCommand: TPOCommand,
        tpoHereCommand: TPOHereCommand,
        tpToggleCommand: TPToggleCommand,
        pcbridgeCommand: PCBridgeCommand,
        delWarpCommand: DelWarpCommand,
        setWarpCommand: SetWarpCommand,
        warpCommand: WarpCommand,
        warpsCommand: WarpsCommand,
    ) {
        val commands: List<SpigotCommand> = listOf(
            banCommand,
            banIPCommand,
            unbanCommand,
            unbanIPCommand,
            checkBanCommand,
            hubCommand,
            setHubCommand,
            aCommand,
            ignoreCommand,
            muteCommand,
            replyCommand,
            unignoreCommand,
            unmuteCommand,
            whisperCommand,
            syncCommand,
            syncOtherCommand,
            backCommand,
            tpCommand,
            tpHereCommand,
            tpaCommand,
            tpaHereCommand,
            tpAcceptCommand,
            tpDenyCommand,
            tpoCommand,
            tpoHereCommand,
            tpToggleCommand,
            pcbridgeCommand,
            delWarpCommand,
            setWarpCommand,
            warpCommand,
            warpsCommand,
        )
    }

    class Listeners @Inject constructor(
        banConnectionListener: BanConnectionListener,
        chatListener: ChatListener,
        serverJoinMessageListener: ServerJoinMessageListener,
        welcomeMessageListener: WelcomeMessageListener,
        firstTimeJoinMessageListener: FirstTimeJoinMessageListener,
        syncRankLoginListener: SyncRankLoginListener,
        playerPreTeleportListener: PlayerPreTeleportListener,
    ) {
        val listeners: List<SpigotListener> = listOf(
            banConnectionListener,
            chatListener,
            serverJoinMessageListener,
            welcomeMessageListener,
            firstTimeJoinMessageListener,
            syncRankLoginListener,
            playerPreTeleportListener,
        )
    }

    class Integrations @Inject constructor(
        dynmapIntegrationModule: DynmapIntegrationModule,
        gadgetsMenuIntegrationModule: GadgetsMenuIntegrationModule,
    ) {
        val integrations: List<SpigotFeatureModule> = listOf(
            dynmapIntegrationModule,
            gadgetsMenuIntegrationModule,
        )
    }
}
