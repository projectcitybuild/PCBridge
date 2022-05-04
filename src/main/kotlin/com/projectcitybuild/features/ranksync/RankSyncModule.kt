package com.projectcitybuild.features.ranksync

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.ranksync.commands.SyncCommand
import com.projectcitybuild.features.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.features.ranksync.listeners.SyncRankLoginListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject

class RankSyncModule @Inject constructor(
    syncCommand: SyncCommand,
    syncOtherCommand: SyncOtherCommand,
    syncRankLoginListener: SyncRankLoginListener,
    config: PlatformConfig,
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            syncCommand,
            syncOtherCommand,
        )
        else emptyArray()

    override val spigotListeners: Array<SpigotListener> =
        if (config.get(ConfigKey.API_ENABLED)) arrayOf(
            syncRankLoginListener,
        )
        else emptyArray()
}
