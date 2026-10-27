package com.projectcitybuild.pcbridge.features.teleportation

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.FindRandomLocation
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportRequestService
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.features.teleportation.domain.validation.TeleportValidator
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportAcceptCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportAllCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportBackCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportCancelCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportDenyCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportHereCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportLegacyCommands
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportPosCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportRandomCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportSendCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.TeleportToCommand
import com.projectcitybuild.pcbridge.features.teleportation.hooks.listeners.TeleportRequestListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val teleportationModule =
    module {
        paperFeature("teleportation") {
            commands(
                get<TeleportCommand>(),
                *get<TeleportLegacyCommands>().commands().toTypedArray(),
            )
            listeners(get<TeleportRequestListener>())
        }

        factoryOf(::TeleportLegacyCommands)
        factoryOf(::TeleportCommand)
        factoryOf(::TeleportToCommand)
        factoryOf(::TeleportHereCommand)
        factoryOf(::TeleportSendCommand)
        factoryOf(::TeleportAllCommand)
        factoryOf(::TeleportPosCommand)
        factoryOf(::TeleportRandomCommand)
        factoryOf(::TeleportBackCommand)
        factoryOf(::TeleportAcceptCommand)
        factoryOf(::TeleportDenyCommand)
        factoryOf(::TeleportCancelCommand)
        singleOf(::TeleportService)
        single { TeleportRequestService() }
        factoryOf(::TeleportValidator)
        factoryOf(::TeleportRequestListener)
        factory { FindRandomLocation(constraintProvider = get()) }
    }
