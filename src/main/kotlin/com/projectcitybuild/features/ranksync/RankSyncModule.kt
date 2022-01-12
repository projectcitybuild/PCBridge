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

class RankSyncModule(
    proxyServer: ProxyServer,
    apiRequestFactory: APIRequestFactory,
    apiClient: APIClient,
    syncPlayerGroupService: SyncPlayerGroupService,
    nameGuesser: NameGuesser
): BungeecordFeatureModule {

    override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        SyncCommand(apiRequestFactory, apiClient, syncPlayerGroupService),
        SyncOtherCommand(proxyServer, syncPlayerGroupService, nameGuesser),
    )

    override val bungeecordListeners: Array<Listener> = arrayOf(
        SyncRankLoginListener(syncPlayerGroupService),
    )
}