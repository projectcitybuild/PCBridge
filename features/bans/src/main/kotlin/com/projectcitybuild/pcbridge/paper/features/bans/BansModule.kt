package com.projectcitybuild.pcbridge.paper.features.bans

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CheckBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CreateUuidBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.repositories.UuidBanRepository
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanDialogListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val bansModule =
    module {
        paperFeature("bans") {
            commands(get<BanCommand>())
            listeners(
                get<BanWebhookListener>(),
                get<BanDialogListener>(),
            )
            connectionMiddleware(get<BanConnectionMiddleware>(), priority = 100)
        }

        factoryOf(::CheckBan)
        factoryOf(::BanConnectionMiddleware)
        factoryOf(::BanWebhookListener)
        factoryOf(::BanDialogListener)
        factoryOf(::BanCommand)

        single {
            UuidBanRepository(
                uuidBanHttpService = get<PCBHttp>().uuidBans,
            )
        }

        factoryOf(::CreateUuidBan)
    }
