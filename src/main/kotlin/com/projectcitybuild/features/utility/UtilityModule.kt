package com.projectcitybuild.features.utility

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.utility.commands.PCBridgeCommand
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject

class UtilityModule @Inject constructor(
    PCBridgeCommand: PCBridgeCommand
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        PCBridgeCommand,
    )
}
