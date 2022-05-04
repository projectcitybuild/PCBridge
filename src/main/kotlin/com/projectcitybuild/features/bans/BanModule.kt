package com.projectcitybuild.features.bans

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.BanIPCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.features.bans.commands.UnbanIPCommand
import com.projectcitybuild.features.bans.listeners.BanConnectionListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject

class BanModule @Inject constructor(
    banCommand: BanCommand,
    banIPCommand: BanIPCommand,
    unbanCommand: UnbanCommand,
    unbanIPCommand: UnbanIPCommand,
    checkBanCommand: CheckBanCommand,
    banConnectionListener: BanConnectionListener,
    config: PlatformConfig,
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            banCommand,
            banIPCommand,
            unbanCommand,
            unbanIPCommand,
            checkBanCommand,
        )
        else emptyArray()

    override val spigotListeners: Array<SpigotListener> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            banConnectionListener,
        )
        else emptyArray()
}
