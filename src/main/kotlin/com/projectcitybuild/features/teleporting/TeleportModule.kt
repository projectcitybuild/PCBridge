package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporting.commands.TPACommand
import com.projectcitybuild.features.teleporting.commands.TPAHereCommand
import com.projectcitybuild.features.teleporting.commands.TPAcceptCommand
import com.projectcitybuild.features.teleporting.commands.TPCommand
import com.projectcitybuild.features.teleporting.commands.TPDenyCommand
import com.projectcitybuild.features.teleporting.commands.TPHereCommand
import com.projectcitybuild.features.teleporting.commands.TPOCommand
import com.projectcitybuild.features.teleporting.commands.TPOHereCommand
import com.projectcitybuild.features.teleporting.commands.TPToggleCommand
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject

class TeleportModule @Inject constructor(
    tpCommand: TPCommand,
    tpHereCommand: TPHereCommand,
    tpaCommand: TPACommand,
    tpaHereCommand: TPAHereCommand,
    tpAcceptCommand: TPAcceptCommand,
    tpDenyCommand: TPDenyCommand,
    tpoCommand: TPOCommand,
    tpoHereCommand: TPOHereCommand,
    tpToggleCommand: TPToggleCommand,
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        tpCommand,
        tpHereCommand,
        tpaCommand,
        tpaHereCommand,
        tpAcceptCommand,
        tpDenyCommand,
        tpoCommand,
        tpoHereCommand,
        tpToggleCommand,
    )
}
