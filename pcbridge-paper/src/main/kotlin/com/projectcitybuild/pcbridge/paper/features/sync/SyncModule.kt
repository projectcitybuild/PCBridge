package com.projectcitybuild.pcbridge.paper.features.sync

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.architecture.PlayerDataProvider
import com.projectcitybuild.pcbridge.paper.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncDebugCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.sync.domain.repositories.PlayerRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.bind
import org.koin.dsl.module

val syncModule = module {
    factory {
        SyncPlayer(
            session = get(),
            time = get(),
            server = get(),
            eventBroadcaster = get(),
            playerRepository = get(),
        )
    }

    factory {
        SyncCommand(
            plugin = get<JavaPlugin>(),
            syncPlayer = get(),
        )
    }

    factory {
        SyncDebugCommand(
            plugin = get<JavaPlugin>(),
            permissions = get(),
        )
    }

    factory {
        PlayerSyncRequestListener(
            syncPlayer = get(),
        )
    }

    factory {
        PlayerRepository(
            httpService = get<PCBHttp>().player,
        )
    }.bind<PlayerDataProvider>()
}