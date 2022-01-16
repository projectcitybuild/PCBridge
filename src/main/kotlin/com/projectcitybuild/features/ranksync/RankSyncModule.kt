package com.projectcitybuild.features.ranksync

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.ranksync.commands.SyncCommand
import com.projectcitybuild.features.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.features.ranksync.listeners.SyncRankLoginListener
import com.projectcitybuild.modules.nameguesser.NameGuesser
import net.md_5.bungee.api.ProxyServer
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