package com.projectcitybuild.plugin

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotIntegration
import com.projectcitybuild.integrations.dynmap.DynmapMarkerIntegration
import com.projectcitybuild.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.integrations.gadgetsmenu.GadgetsMenuIntegration
import com.projectcitybuild.plugin.commands.ACommand
import com.projectcitybuild.plugin.commands.BanCommand
import com.projectcitybuild.plugin.commands.BanIPCommand
import com.projectcitybuild.plugin.commands.CheckBanCommand
import com.projectcitybuild.plugin.commands.DelWarpCommand
import com.projectcitybuild.plugin.commands.MuteCommand
import com.projectcitybuild.plugin.commands.PCBridgeCommand
import com.projectcitybuild.plugin.commands.SetWarpCommand
import com.projectcitybuild.plugin.commands.SyncCommand
import com.projectcitybuild.plugin.commands.SyncOtherCommand
import com.projectcitybuild.plugin.commands.UnbanCommand
import com.projectcitybuild.plugin.commands.UnbanIPCommand
import com.projectcitybuild.plugin.commands.UnmuteCommand
import com.projectcitybuild.plugin.commands.WarpCommand
import com.projectcitybuild.plugin.commands.WarpsCommand
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.listeners.BanConnectionListener
import com.projectcitybuild.plugin.listeners.ChatListener
import com.projectcitybuild.plugin.listeners.FirstTimeJoinMessageListener
import com.projectcitybuild.plugin.listeners.PlayerCacheListener
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

    val integrations: List<SpigotIntegration>
        get() = _integrations.enabled

    class Integrations @Inject constructor(
        dynmapIntegration: DynmapMarkerIntegration,
        essentialsIntegration: EssentialsIntegration,
        gadgetsMenuIntegration: GadgetsMenuIntegration,
    ) {
        val enabled: List<SpigotIntegration> = listOf(
            dynmapIntegration,
            essentialsIntegration,
            gadgetsMenuIntegration,
        )
    }

    class Commands @Inject constructor(
        aCommand: ACommand,
        banCommand: BanCommand,
        banIPCommand: BanIPCommand,
        checkBanCommand: CheckBanCommand,
        delWarpCommand: DelWarpCommand,
        muteCommand: MuteCommand,
        pcbridgeCommand: PCBridgeCommand,
        setWarpCommand: SetWarpCommand,
        syncCommand: SyncCommand,
        syncOtherCommand: SyncOtherCommand,
        unbanCommand: UnbanCommand,
        unbanIPCommand: UnbanIPCommand,
        unmuteCommand: UnmuteCommand,
        warpCommand: WarpCommand,
        warpsCommand: WarpsCommand,
    ) {
        val enabled: List<SpigotCommand> = listOf(
            aCommand,
            banCommand,
            banIPCommand,
            checkBanCommand,
            delWarpCommand,
            muteCommand,
            pcbridgeCommand,
            setWarpCommand,
            syncCommand,
            syncOtherCommand,
            unbanCommand,
            unbanIPCommand,
            unmuteCommand,
            warpCommand,
            warpsCommand,
        )
    }

    class Listeners @Inject constructor(
        banConnectionListener: BanConnectionListener,
        chatListener: ChatListener,
        firstTimeJoinMessageListener: FirstTimeJoinMessageListener,
        playerCacheListener: PlayerCacheListener,
        serverJoinMessageListener: ServerJoinMessageListener,
        syncRankLoginListener: SyncRankLoginListener,
        welcomeMessageListener: WelcomeMessageListener,
    ) {
        val enabled: List<SpigotListener> = listOf(
            banConnectionListener,
            chatListener,
            firstTimeJoinMessageListener,
            playerCacheListener,
            serverJoinMessageListener,
            syncRankLoginListener,
            welcomeMessageListener,
        )
    }
}
