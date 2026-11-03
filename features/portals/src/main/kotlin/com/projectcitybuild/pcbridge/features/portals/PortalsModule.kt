package com.projectcitybuild.pcbridge.features.portals

import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.PortalLinksRepository
import com.projectcitybuild.pcbridge.features.portals.hooks.commands.PortalsCommand
import com.projectcitybuild.pcbridge.features.portals.hooks.listeners.PortalListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val portalsModule =
    module {
        paperFeature("portals") {
            commands(get<PortalsCommand>())
            listeners(get<PortalListener>())
        }

        single {
            val plugin = get<JavaPlugin>()
            PortalSettings.load(
                plugin.dataFolder.parentFile.toPath()
                    .resolve("Multiverse-NetherPortals/config.yml"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            PortalLinksRepository(
                linksFile =
                    plugin.dataFolder.parentFile.toPath()
                        .resolve("Multiverse-NetherPortals/links.yml"),
                settings = get(),
                worldDefinitionRepository = get(),
                multiverseNetherPortalsEnabled = {
                    plugin.server.pluginManager.isPluginEnabled("Multiverse-NetherPortals")
                },
            )
        }
        factoryOf(::PortalsCommand)
        factoryOf(::PortalListener)
    }
