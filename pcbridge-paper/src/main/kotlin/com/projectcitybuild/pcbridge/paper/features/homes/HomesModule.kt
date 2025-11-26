package com.projectcitybuild.pcbridge.paper.features.homes

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomeCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomeNameSuggester
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomesCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes.HomeRenameCommand
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.listeners.HomeRenameDialogListener
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val homesModule = module {
    single {
        HomeRepository(
            homeHttpService = get<PCBHttp>().homes,
        )
    }

    factory {
        HomeNameSuggester(
            homeRepository = get(),
        )
    }

    factory {
        HomeCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            playerTeleporter = get(),
            homeNameSuggester = get(),
            homeRepository = get(),
        )
    }

    factory {
        HomesCommand(
            homeListCommand = HomeListCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
                remoteConfig = get(),
            ),
            homeCreateCommand = HomeCreateCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeMoveCommand = HomeMoveCommand(
                plugin = get<JavaPlugin>(),
                homeNameSuggester = get(),
                homeRepository = get(),
            ),
            homeDeleteCommand = HomeDeleteCommand(
                plugin = get<JavaPlugin>(),
                homeNameSuggester = get(),
                homeRepository = get(),
            ),
            homeLimitCommand = HomeLimitCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeRenameCommand = HomeRenameCommand(
                plugin = get<JavaPlugin>(),
                homeNameSuggester = get(),
                homeRepository = get(),
            ),
        )
    }

    factory {
        HomeRenameDialogListener(
            homeRepository = get(),
            errorTracker = get(),
        )
    }
}