package com.projectcitybuild.features.bans

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.BanIPCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.features.bans.commands.UnbanIPCommand
import com.projectcitybuild.features.bans.listeners.BanConnectionListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class BanModule @Inject constructor(
    banCommand: BanCommand,
    banIPCommand: BanIPCommand,
    unbanCommand: UnbanCommand,
    unbanIPCommand: UnbanIPCommand,
    checkBanCommand: CheckBanCommand,
    banConnectionListener: BanConnectionListener,
    config: PlatformConfig,
) : BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            banCommand,
            banIPCommand,
            unbanCommand,
            unbanIPCommand,
            checkBanCommand,
        )
        else emptyArray()

    override val bungeecordListeners: Array<Listener> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            banConnectionListener,
        )
        else emptyArray()
}
