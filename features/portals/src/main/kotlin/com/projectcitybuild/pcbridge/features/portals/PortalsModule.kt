package com.projectcitybuild.pcbridge.features.portals

import com.projectcitybuild.pcbridge.features.portals.domain.data.MultiversePortalsSettings
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.CustomPortalsRepository
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.PortalLinksRepository
import com.projectcitybuild.pcbridge.features.portals.domain.services.CustomPortalService
import com.projectcitybuild.pcbridge.features.portals.hooks.commands.CustomPortalsCommand
import com.projectcitybuild.pcbridge.features.portals.hooks.commands.PortalsCommand
import com.projectcitybuild.pcbridge.features.portals.hooks.listeners.CustomPortalListener
import com.projectcitybuild.pcbridge.features.portals.hooks.listeners.PortalListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val portalsModule =
    module {
        paperFeature("portals") {
            commands(get<PortalsCommand>(), get<CustomPortalsCommand>())
            listeners(get<PortalListener>(), get<CustomPortalListener>())
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
            MultiversePortalsSettings.load(
                plugin.dataFolder.parentFile.toPath()
                    .resolve("Multiverse-Portals/config.yml"),
            )
        }
        single {
            val plugin = get<JavaPlugin>()
            CustomPortalsRepository(
                portalsFile = plugin.dataFolder.parentFile.toPath()
                    .resolve("Multiverse-Portals/portals.yml"),
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
            )
        }
        factoryOf(::PortalsCommand)
        factoryOf(::PortalListener)
        factoryOf(::CustomPortalsCommand)
        factoryOf(::CustomPortalListener)
        single {
            CustomPortalService(
                plugin = get(),
                server = get(),
                repository = get(),
                settings = get(),
                worldLifecycleService = get(),
                playerTeleporter = get(),
            )
        }
    }
