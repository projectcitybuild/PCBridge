package com.projectcitybuild.pcbridge.paper.features.warps

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners.WarpRenameDialogListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val warpsModule = module {
    single {
        WarpRepository(
            warpHttpService = get<PCBHttp>().warps
        )
    }

    factory {
        WarpNameSuggester(
            warpRepository = get(),
        )
    }

    factory {
        WarpCommand(
            plugin = get<JavaPlugin>(),
            warpNameSuggester = get(),
            warpRepository = get(),
            server = get(),
            playerTeleporter = get(),
        )
    }

    factory {
        WarpsCommand(
            createCommand = WarpCreateCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                eventBroadcaster = get(),
            ),
            deleteCommand = WarpDeleteCommand(
                plugin = get<JavaPlugin>(),
                warpNameSuggester = get(),
                warpRepository = get(),
                eventBroadcaster = get(),
            ),
            listCommand = WarpListCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                remoteConfig = get(),
            ),
            moveCommand = WarpMoveCommand(
                plugin = get<JavaPlugin>(),
                warpNameSuggester = get(),
                warpRepository = get(),
            ),
            renameCommand = WarpRenameCommand(
                plugin = get<JavaPlugin>(),
                warpNameSuggester = get(),
                warpRepository = get(),
            ),
        )
    }

    factory {
        WarpWebhookListener(
            warpRepository = get(),
        )
    }

    factory {
        WarpRenameDialogListener(
            warpRepository = get(),
            errorTracker = get(),
        )
    }
}