package com.projectcitybuild.features.warps

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.warps.adapters.dynmap.DynmapMarkerAdapter
import com.projectcitybuild.features.warps.commands.DelWarpCommand
import com.projectcitybuild.features.warps.commands.SetWarpCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.features.warps.listeners.WarpOnJoinListener
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class WarpModule @Inject constructor(
    delWarpCommand: DelWarpCommand,
    setWarpCommand: SetWarpCommand,
    warpCommand: WarpCommand,
    warpsCommand: WarpsCommand,
    warpOnJoinListener: WarpOnJoinListener,
    private val dynmapMarkerAdapter: DynmapMarkerAdapter,
): SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        delWarpCommand,
        setWarpCommand,
        warpCommand,
        warpsCommand,
    )

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        warpOnJoinListener,
    )

    override fun onEnable() {
        dynmapMarkerAdapter.enable()
    }
}