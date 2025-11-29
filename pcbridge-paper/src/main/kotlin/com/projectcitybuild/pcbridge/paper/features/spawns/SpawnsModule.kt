package com.projectcitybuild.pcbridge.paper.features.spawns

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.paper.core.libs.storage.JsonStorage
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.HubCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.data.SerializableSpawn
import com.projectcitybuild.pcbridge.paper.features.spawns.hooks.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.repositories.SpawnRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val spawnsModule = module {
    factory {
        SpawnCommand(
            plugin = get<JavaPlugin>(),
            spawnRepository = get(),
            playerTeleporter = get(),
        )
    }

    factory {
        SetSpawnCommand(
            plugin = get<JavaPlugin>(),
            spawnRepository = get(),
            eventBroadcaster = get(),
        )
    }

    factory {
        HubCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            remoteConfig = get(),
            playerTeleporter = get(),
        )
    }

    factory {
        PlayerRespawnListener(
            spawnRepository = get(),
        )
    }

    single {
        SpawnRepository(
            storage = JsonStorage(
                typeToken = object : TypeToken<SerializableSpawn>() {},
            ),
            server = get(),
        )
    }
}