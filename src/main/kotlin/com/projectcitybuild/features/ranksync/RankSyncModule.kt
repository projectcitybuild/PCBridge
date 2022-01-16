package com.projectcitybuild.features.ranksync

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.ranksync.commands.SyncCommand
import com.projectcitybuild.features.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.features.ranksync.listeners.SyncRankLoginListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class RankSyncModule @Inject constructor(
    syncCommand: SyncCommand,
    syncOtherCommand: SyncOtherCommand,
    syncRankLoginListener: SyncRankLoginListener,
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        syncCommand,
        syncOtherCommand,
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        syncRankLoginListener,
    )
}