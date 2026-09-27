package com.projectcitybuild.pcbridge.paper.features.spawns

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.data.SerializableSpawn
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.HubCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.listeners.PlayerFirstJoinSpawnListener
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val spawnsModule =
    module {
        factoryOf(::SpawnCommand)
        factoryOf(::SetSpawnCommand)
        factoryOf(::HubCommand)
        factoryOf(::PlayerRespawnListener)
        factoryOf(::PlayerFirstJoinSpawnListener)

        single {
            SpawnRepository(
                storage =
                    JsonStorage(
                        typeToken = object : TypeToken<SerializableSpawn>() {},
                    ),
                server = get(),
            )
        }

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
    }
