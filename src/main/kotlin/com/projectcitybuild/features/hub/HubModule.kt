package com.projectcitybuild.features.hub

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.hub.commands.HubCommand
import com.projectcitybuild.features.hub.commands.SetHubCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class HubModule @Inject constructor(
    hubCommand: HubCommand,
    setHubCommand: SetHubCommand
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        hubCommand,
        setHubCommand,
    )
}
