package com.projectcitybuild.pcbridge.paper.features.sync

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.features.sync.domain.repositories.ConnectionRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val syncModule = module {
    factory {
        SyncPlayer(
            session = get(),
            time = get(),
            server = get(),
            eventBroadcaster = get(),
            connectionRepository = get(),
        )
    }

    factory {
        SyncCommand(
            plugin = get<JavaPlugin>(),
            syncPlayer = get(),
        )
    }

    factory {
        PlayerSyncRequestListener(
            syncPlayer = get(),
        )
    }

    factory {
        ConnectionRepository(
            httpService = get<PCBHttp>().connection,
        )
    }
}