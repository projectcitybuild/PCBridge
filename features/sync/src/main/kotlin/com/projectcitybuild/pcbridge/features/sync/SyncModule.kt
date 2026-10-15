package com.projectcitybuild.pcbridge.features.sync

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.PCBHttp
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.features.sync.hooks.commands.SyncCommand
import com.projectcitybuild.pcbridge.features.sync.hooks.listener.PlayerSyncRequestListener
import com.projectcitybuild.pcbridge.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
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
