package com.projectcitybuild.pcbridge.paper.features.bans

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.http.playerdb.PlayerDbHttp
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CheckBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CreateUuidBan
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanDialogListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.middleware.BanConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.features.bans.domain.repositories.UuidBanRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val bansModule = module {
    factory {
        BanConnectionMiddleware(
            checkBan = CheckBan(),
        )
    }

    factory {
        BanWebhookListener(
            server = get(),
        )
    }

    factory {
        BanDialogListener(
            server = get(),
            createUuidBan = get(),
        )
    }

    factory {
        BanCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
        )
    }

    single {
        UuidBanRepository(
            uuidBanHttpService = get<PCBHttp>().uuidBans,
        )
    }

    factory {
        CreateUuidBan(
            playerLookup = get(),
            manageUrlGenerator = get(),
            uuidBanRepository = get(),
        )
    }
}