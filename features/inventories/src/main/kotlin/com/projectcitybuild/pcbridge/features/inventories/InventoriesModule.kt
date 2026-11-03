package com.projectcitybuild.pcbridge.features.inventories

import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryCompatibilitySettings
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.MultiverseInventoryRepository
import com.projectcitybuild.pcbridge.features.inventories.domain.services.MultiverseInventoryTransitionService
import com.projectcitybuild.pcbridge.features.inventories.hooks.commands.InventoriesCommand
import com.projectcitybuild.pcbridge.features.inventories.hooks.listeners.MultiverseInventoryTransitionListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val inventoriesModule =
    module {
        paperFeature("inventories") {
            commands(get<InventoriesCommand>())
            listeners(get<MultiverseInventoryTransitionListener>())
        }

        factoryOf(::InventoriesCommand)

        single {
            val plugin = get<JavaPlugin>()
            InventoryCompatibilitySettings.load(
                plugin.dataFolder.resolve("inventory-compatibility.json"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            MultiverseInventoryConfig.load(
                plugin.dataFolder.parentFile.toPath()
                    .resolve("Multiverse-Inventories/config.yml"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            MultiverseInventoryRepository(
                dataDirectory = plugin.dataFolder.parentFile.toPath().resolve("Multiverse-Inventories"),
                backupDirectory = plugin.dataFolder.toPath().resolve("backups"),
                mode = get<InventoryCompatibilitySettings>().mode,
                multiverseInventoriesEnabled = {
                    plugin.server.pluginManager.isPluginEnabled("Multiverse-Inventories")
                },
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            MultiverseInventoryTransitionService(
                repository = get(),
                config = get<MultiverseInventoryConfig>(),
                mode = get<InventoryCompatibilitySettings>().mode,
                multiverseInventoriesEnabled = {
                    plugin.server.pluginManager.isPluginEnabled("Multiverse-Inventories")
                },
            )
        }
        factoryOf(::MultiverseInventoryTransitionListener)
    }
