package com.projectcitybuild.features.ranksync

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.features.ranksync.commands.SyncCommand
import com.projectcitybuild.features.ranksync.commands.SyncOtherCommand
import com.projectcitybuild.features.ranksync.listeners.SyncRankLoginListener
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener

class RankSyncModule(
    proxyServer: ProxyServer,
    apiRequestFactory: APIRequestFactory,
    apiClient: APIClient,
    syncPlayerGroupService: SyncPlayerGroupService
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        SyncCommand(apiRequestFactory, apiClient, syncPlayerGroupService),
        SyncOtherCommand(proxyServer, syncPlayerGroupService),
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        SyncRankLoginListener(syncPlayerGroupService),
    )
}