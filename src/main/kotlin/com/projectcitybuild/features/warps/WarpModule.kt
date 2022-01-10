package com.projectcitybuild.features.warps

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.warps.commands.DelWarpCommand
import com.projectcitybuild.features.warps.commands.SetWarpCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.features.warps.listeners.IncomingSetWarpListener
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener
import org.bukkit.plugin.Plugin

class WarpModule {

    class Bungeecord(
        proxyServer: ProxyServer,
        warpFileStorage: WarpFileStorage
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            DelWarpCommand(warpFileStorage),
            WarpCommand(proxyServer, warpFileStorage),
            WarpsCommand(warpFileStorage),
        )

        override val bungeecordListeners: Array<Listener> = arrayOf(
            IncomingSetWarpListener(warpFileStorage),
        )
    }

    class Spigot(plugin: Plugin): SpigotFeatureModule {
        override val spigotCommands: Array<SpigotCommand> = arrayOf(
            SetWarpCommand(plugin),
        )
    }
}