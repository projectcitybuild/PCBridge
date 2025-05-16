package com.projectcitybuild.pcbridge.paper.features.homes

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomeCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomesCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeRenameCommand
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val homesModule = module {
    single {
        HomeRepository(
            homeHttpService = get<PCBHttp>().homes,
        )
    }

    factory {
        HomeCommand(
            plugin = get<JavaPlugin>(),
            server = get(),
            playerTeleporter = get(),
            homeRepository = get(),
        )
    }

    factory {
        HomesCommand(
            homeListCommand = HomeListCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeCreateCommand = HomeCreateCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeMoveCommand = HomeMoveCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeDeleteCommand = HomeDeleteCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeLimitCommand = HomeLimitCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
            homeRenameCommand = HomeRenameCommand(
                plugin = get<JavaPlugin>(),
                homeRepository = get(),
            ),
        )
    }
}