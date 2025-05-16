package com.projectcitybuild.pcbridge.paper.features.config

import com.projectcitybuild.pcbridge.paper.features.config.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.features.config.listeners.ConfigWebhookListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val configModule = module {
    factory {
        ConfigCommand(
            plugin = get<JavaPlugin>(),
            remoteConfig = get(),
        )
    }

    factory {
        ConfigWebhookListener(
            remoteConfig = get(),
        )
    }
}