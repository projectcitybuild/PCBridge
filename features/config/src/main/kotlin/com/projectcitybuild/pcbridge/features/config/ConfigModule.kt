package com.projectcitybuild.pcbridge.features.config

import com.projectcitybuild.pcbridge.features.config.hooks.commands.ConfigCommand
import com.projectcitybuild.pcbridge.features.config.hooks.listeners.ConfigWebhookListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val configModule =
    module {
        paperFeature("config") {
            commands(get<ConfigCommand>())
            listeners(get<ConfigWebhookListener>())
        }

        factoryOf(::ConfigCommand)
        factoryOf(::ConfigWebhookListener)
    }
