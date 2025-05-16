package com.projectcitybuild.pcbridge.paper.features.warps

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpsCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpReloadCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.paper.features.warps.listeners.WarpWebhookListener
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val warpsModule = module {
    single {
        WarpRepository(
            warpHttpService = get<PCBHttp>().warps
        )
    }

    factory {
        WarpCommand(
            plugin = get<JavaPlugin>(),
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
                server = get(),
            ),
            deleteCommand = WarpDeleteCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                server = get(),
            ),
            listCommand = WarpListCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
                remoteConfig = get(),
            ),
            moveCommand = WarpMoveCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
            reloadCommand = WarpReloadCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
            renameCommand = WarpRenameCommand(
                plugin = get<JavaPlugin>(),
                warpRepository = get(),
            ),
        )
    }

    factory {
        WarpWebhookListener(
            warpRepository = get(),
        )
    }
}