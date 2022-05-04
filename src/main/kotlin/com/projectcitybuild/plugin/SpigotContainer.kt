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
import com.projectcitybuild.plugin.listeners.PlayerCacheListener
import com.projectcitybuild.plugin.listeners.PlayerPreTeleportListener
import com.projectcitybuild.plugin.listeners.ServerJoinMessageListener
import com.projectcitybuild.plugin.listeners.SyncRankLoginListener
import com.projectcitybuild.plugin.listeners.WelcomeMessageListener
import javax.inject.Inject

class SpigotContainer @Inject constructor(
    private val _commands: Commands,
    private val _listeners: Listeners,
    private val _integrations: Integrations,
) {
    val commands: List<SpigotCommand>
        get() = _commands.enabled

    val listeners: List<SpigotListener>
        get() = _listeners.enabled

    val integrations: List<SpigotFeatureModule>
        get() = _integrations.enabled

    class Integrations @Inject constructor(
        dynmapIntegrationModule: DynmapIntegrationModule,
        gadgetsMenuIntegrationModule: GadgetsMenuIntegrationModule,
    ) {
        val enabled: List<SpigotFeatureModule> = listOf(
            dynmapIntegrationModule,
            gadgetsMenuIntegrationModule,
        )
    }

    class Commands @Inject constructor(
        aCommand: ACommand,
        backCommand: BackCommand,
        banCommand: BanCommand,
        banIPCommand: BanIPCommand,
        checkBanCommand: CheckBanCommand,
        delWarpCommand: DelWarpCommand,
        hubCommand: HubCommand,
        ignoreCommand: IgnoreCommand,
        muteCommand: MuteCommand,
        pcbridgeCommand: PCBridgeCommand,
        replyCommand: ReplyCommand,
        setHubCommand: SetHubCommand,
        setWarpCommand: SetWarpCommand,
        syncCommand: SyncCommand,
        syncOtherCommand: SyncOtherCommand,
        tpAcceptCommand: TPAcceptCommand,
        tpCommand: TPCommand,
        tpDenyCommand: TPDenyCommand,
        tpHereCommand: TPHereCommand,
        tpToggleCommand: TPToggleCommand,
        tpaCommand: TPACommand,
        tpaHereCommand: TPAHereCommand,
        tpoCommand: TPOCommand,
        tpoHereCommand: TPOHereCommand,
        unbanCommand: UnbanCommand,
        unbanIPCommand: UnbanIPCommand,
        unignoreCommand: UnignoreCommand,
        unmuteCommand: UnmuteCommand,
        warpCommand: WarpCommand,
        warpsCommand: WarpsCommand,
        whisperCommand: WhisperCommand,
    ) {
        val enabled: List<SpigotCommand> = listOf(
            aCommand,
            backCommand,
            banCommand,
            banIPCommand,
            checkBanCommand,
            delWarpCommand,
            hubCommand,
            ignoreCommand,
            muteCommand,
            pcbridgeCommand,
            replyCommand,
            setHubCommand,
            setWarpCommand,
            syncCommand,
            syncOtherCommand,
            tpAcceptCommand,
            tpCommand,
            tpDenyCommand,
            tpHereCommand,
            tpToggleCommand,
            tpaCommand,
            tpaHereCommand,
            tpoCommand,
            tpoHereCommand,
            unbanCommand,
            unbanIPCommand,
            unignoreCommand,
            unmuteCommand,
            warpCommand,
            warpsCommand,
            whisperCommand,
        )
    }

    class Listeners @Inject constructor(
        banConnectionListener: BanConnectionListener,
        chatListener: ChatListener,
        firstTimeJoinMessageListener: FirstTimeJoinMessageListener,
        playerCacheListener: PlayerCacheListener,
        playerPreTeleportListener: PlayerPreTeleportListener,
        serverJoinMessageListener: ServerJoinMessageListener,
        syncRankLoginListener: SyncRankLoginListener,
        welcomeMessageListener: WelcomeMessageListener,
    ) {
        val enabled: List<SpigotListener> = listOf(
            banConnectionListener,
            chatListener,
            firstTimeJoinMessageListener,
            playerCacheListener,
            playerPreTeleportListener,
            serverJoinMessageListener,
            syncRankLoginListener,
            welcomeMessageListener,
        )
    }
}
