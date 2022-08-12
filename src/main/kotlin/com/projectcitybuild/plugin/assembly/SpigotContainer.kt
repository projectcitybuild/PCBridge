package com.projectcitybuild.plugin.assembly

import com.projectcitybuild.core.SpigotListener
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
import com.projectcitybuild.plugin.integrations.SpigotIntegration
import com.projectcitybuild.plugin.integrations.dynmap.DynmapMarkerIntegration
import com.projectcitybuild.plugin.integrations.essentials.EssentialsIntegration
import com.projectcitybuild.plugin.integrations.gadgetsmenu.GadgetsMenuIntegration
import com.projectcitybuild.plugin.integrations.luckperms.LuckPermsIntegration
import com.projectcitybuild.plugin.listeners.AsyncPlayerChatListener
import com.projectcitybuild.plugin.listeners.AsyncPreLoginListener
import com.projectcitybuild.plugin.listeners.FirstTimeJoinListener
import com.projectcitybuild.plugin.listeners.PlayerJoinListener
import com.projectcitybuild.plugin.listeners.PlayerQuitEvent
import com.projectcitybuild.plugin.listeners.TelemetryListener
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
        luckPermsIntegration: LuckPermsIntegration,
    ) {
        val enabled: List<SpigotIntegration> = listOf(
            dynmapIntegration,
            essentialsIntegration,
            gadgetsMenuIntegration,
            luckPermsIntegration,
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
        asyncPlayerChatListener: AsyncPlayerChatListener,
        firstTimeJoinListener: FirstTimeJoinListener,
        asyncPreLoginListener: AsyncPreLoginListener,
        playerJoinListener: PlayerJoinListener,
        playerQuitEvent: PlayerQuitEvent,
        telemetryListener: TelemetryListener,
    ) {
        val enabled: List<SpigotListener> = listOf(
            asyncPlayerChatListener,
            firstTimeJoinListener,
            asyncPreLoginListener,
            playerJoinListener,
            playerQuitEvent,
            telemetryListener,
        )
    }
}
