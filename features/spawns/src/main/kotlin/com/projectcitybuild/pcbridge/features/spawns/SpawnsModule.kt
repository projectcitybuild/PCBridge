package com.projectcitybuild.pcbridge.features.spawns

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.features.spawns.domain.data.SerializableSpawn
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.features.spawns.hooks.commands.HubCommand
import com.projectcitybuild.pcbridge.features.spawns.hooks.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.features.spawns.hooks.commands.SpawnCommand
import com.projectcitybuild.pcbridge.features.spawns.hooks.listeners.PlayerFirstJoinSpawnListener
import com.projectcitybuild.pcbridge.features.spawns.hooks.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val spawnsModule =
    module {
        paperFeature("spawns") {
            commands(
                get<HubCommand>(),
                get<SetSpawnCommand>(),
                get<SpawnCommand>(),
            )
            listeners(
                get<PlayerFirstJoinSpawnListener>(),
                get<PlayerRespawnListener>(),
            )
        }

        factoryOf(::SpawnCommand)
        factoryOf(::SetSpawnCommand)
        factoryOf(::HubCommand)
        factoryOf(::PlayerRespawnListener)
        factoryOf(::PlayerFirstJoinSpawnListener)

        single {
            SpawnRepository(
                fileStorage =
                    JsonFileStorage(
                        typeToken = object : TypeToken<SerializableSpawn>() {},
                    ),
                server = get(),
            )
        }
    }
