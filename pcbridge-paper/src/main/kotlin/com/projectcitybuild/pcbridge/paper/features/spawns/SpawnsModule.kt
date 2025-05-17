package com.projectcitybuild.pcbridge.paper.features.spawns

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.JsonStorage
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.HubCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SetSpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.commands.SpawnCommand
import com.projectcitybuild.pcbridge.paper.features.spawns.data.SerializableSpawn
import com.projectcitybuild.pcbridge.paper.features.spawns.listeners.PlayerRespawnListener
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
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