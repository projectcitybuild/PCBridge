package com.projectcitybuild.pcbridge.paper.features.bans

import com.projectcitybuild.pcbridge.paper.features.bans.actions.CheckBan
import com.projectcitybuild.pcbridge.paper.features.bans.commands.BanCommand
import com.projectcitybuild.pcbridge.paper.features.bans.listeners.BanWebhookListener
import com.projectcitybuild.pcbridge.paper.features.bans.middleware.BanConnectionMiddleware
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
        BanCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            manageUrlGenerator = get(),
        )
    }
}