package com.projectcitybuild.pcbridge.paper.features.sync

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.commands.SyncCommand
import com.projectcitybuild.pcbridge.paper.features.sync.hooks.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.paper.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val syncModule =
    module {
        paperFeature("sync") {
            commands(get<SyncCommand>())
            listeners(get<PlayerSyncRequestListener>())
        }

        factoryOf(::SyncPlayer)
        factoryOf(::SyncCommand)
        factoryOf(::PlayerSyncRequestListener)

        factory {
            ConnectionRepository(
                httpService = get<PCBHttp>().connection,
            )
        }
    }
