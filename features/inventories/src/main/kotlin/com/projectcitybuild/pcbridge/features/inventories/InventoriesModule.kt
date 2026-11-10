package com.projectcitybuild.pcbridge.features.inventories

import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.InventoryRepository
import com.projectcitybuild.pcbridge.features.inventories.domain.services.InventoryTransitionService
import com.projectcitybuild.pcbridge.features.inventories.hooks.commands.InventoriesCommand
import com.projectcitybuild.pcbridge.features.inventories.hooks.listeners.InventoryTransitionListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val inventoriesModule =
    module {
        paperFeature("inventories") {
            commands(get<InventoriesCommand>())
            listeners(get<InventoryTransitionListener>())
        }

        factoryOf(::InventoriesCommand)

        single {
            val plugin = get<JavaPlugin>()
            MultiverseInventoryConfig.load(
                plugin.dataFolder.parentFile
                    .toPath()
                    .resolve("Multiverse-Inventories/config.yml"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            InventoryRepository(
                dataDirectory = plugin.dataFolder.parentFile
                    .toPath()
                    .resolve("Multiverse-Inventories"),
                backupDirectory = plugin.dataFolder.toPath().resolve("backups"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            InventoryTransitionService(
                repository = get(),
                config = get<MultiverseInventoryConfig>(),
            )
        }
        factoryOf(::InventoryTransitionListener)
    }
