package com.projectcitybuild.features.homes

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.homes.commands.DelHomeCommand
import com.projectcitybuild.features.homes.commands.HomeCommand
import com.projectcitybuild.features.homes.commands.HomesCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class HomesModule @Inject constructor(
    delHomeCommand: DelHomeCommand,
    homeCommand: HomeCommand,
    homesCommand: HomesCommand,
): SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        delHomeCommand,
        homeCommand,
        homesCommand,
    )
}
