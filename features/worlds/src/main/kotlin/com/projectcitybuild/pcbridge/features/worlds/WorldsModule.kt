package com.projectcitybuild.pcbridge.features.worlds

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.BorderFillCommand
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.BorderSetCommand
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.WorldBorderCommand
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.WorldsCommand
import com.projectcitybuild.pcbridge.features.worlds.hooks.listeners.WorldBorderListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val worldsModule =
    module {
        paperFeature("worlds") {
            commands(get<WorldsCommand>())
            listeners(get<WorldBorderListener>())
        }

        factoryOf(::WorldsCommand)
        factoryOf(::BorderFillCommand)
        factoryOf(::BorderSetCommand)
        factoryOf(::WorldBorderCommand)
        factoryOf(::WorldBorderListener)

        single {
            WorldBorderRepository(
                server = get(),
                fileStorage =
                    JsonFileStorage(
                        typeToken = object : TypeToken<SerializableWorldBorder>() {},
                    ),
            )
        }
    }
