package com.projectcitybuild.pcbridge.paper.features.config

import com.projectcitybuild.pcbridge.paper.features.config.hooks.commands.ConfigCommand
import com.projectcitybuild.pcbridge.paper.features.config.hooks.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val configModule =
    module {
        factoryOf(::ConfigCommand)
        factoryOf(::ConfigWebhookListener)

        paperFeature("config") {
            commands(get<ConfigCommand>())
            listeners(get<ConfigWebhookListener>())
        }
    }
