package com.projectcitybuild.features.utility

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.utility.commands.PCBridgeCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class UtilityModule @Inject constructor(
    PCBridgeCommand: PCBridgeCommand
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        PCBridgeCommand,
    )
}