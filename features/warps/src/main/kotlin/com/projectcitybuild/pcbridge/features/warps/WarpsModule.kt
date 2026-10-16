package com.projectcitybuild.pcbridge.features.warps

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.PCBHttp
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.WarpCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.WarpsCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.listeners.WarpRenameDialogListener
import com.projectcitybuild.pcbridge.features.warps.hooks.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val warpsModule =
    module {
        paperFeature("warps") {
            commands(
                get<WarpCommand>(),
                get<WarpsCommand>(),
            )
            listeners(
                get<WarpRenameDialogListener>(),
                get<WarpWebhookListener>(),
            )
        }

        single {
            WarpRepository(
                warpHttpService = get<PCBHttp>().warps,
            )
        }

        factoryOf(::WarpNameSuggester)
        factoryOf(::WarpCommand)

        factory {
            WarpsCommand(
                createCommand =
                    WarpCreateCommand(
                        plugin = get<JavaPlugin>(),
                        warpRepository = get(),
                        eventBroadcaster = get(),
                    ),
                deleteCommand =
                    WarpDeleteCommand(
                        plugin = get<JavaPlugin>(),
                        warpNameSuggester = get(),
                        warpRepository = get(),
                        eventBroadcaster = get(),
                    ),
                listCommand =
                    WarpListCommand(
                        plugin = get<JavaPlugin>(),
                        warpRepository = get(),
                        remoteConfig = get(),
                    ),
                moveCommand =
                    WarpMoveCommand(
                        plugin = get<JavaPlugin>(),
                        warpNameSuggester = get(),
                        warpRepository = get(),
                    ),
                renameCommand =
                    WarpRenameCommand(
                        plugin = get<JavaPlugin>(),
                        warpNameSuggester = get(),
                        warpRepository = get(),
                    ),
            )
        }

        factoryOf(::WarpWebhookListener)
        factoryOf(::WarpRenameDialogListener)
    }
