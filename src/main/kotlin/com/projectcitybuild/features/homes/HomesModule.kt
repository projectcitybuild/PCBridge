package com.projectcitybuild.features.homes

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.homes.commands.HomeCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class HomesModule @Inject constructor(
    homeCommand: HomeCommand,
): SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        homeCommand,
    )
}
