package com.projectcitybuild.features.ranksync

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.ranksync.commands.SyncCommand
import com.projectcitybuild.features.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.features.ranksync.listeners.SyncRankLoginListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class RankSyncModule @Inject constructor(
    syncCommand: SyncCommand,
    syncOtherCommand: SyncOtherCommand,
    syncRankLoginListener: SyncRankLoginListener,
    config: PlatformConfig,
) : BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            syncCommand,
            syncOtherCommand,
        )
        else emptyArray()

    override val bungeecordListeners: Array<Listener> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            syncRankLoginListener,
        )
        else emptyArray()
}
