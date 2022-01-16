package com.projectcitybuild.features.bans

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.bans.commands.BanCommand
import com.projectcitybuild.features.bans.commands.CheckBanCommand
import com.projectcitybuild.features.bans.commands.UnbanCommand
import com.projectcitybuild.features.bans.listeners.BanConnectionListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class BanModule @Inject constructor(
    banCommand: BanCommand,
    unbanCommand: UnbanCommand,
    checkBanCommand: CheckBanCommand,
    banConnectionListener: BanConnectionListener
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        banCommand,
        unbanCommand,
        checkBanCommand,
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        banConnectionListener,
    )
}