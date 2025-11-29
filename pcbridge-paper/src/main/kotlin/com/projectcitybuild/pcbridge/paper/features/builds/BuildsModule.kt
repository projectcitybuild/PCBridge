package com.projectcitybuild.pcbridge.paper.features.builds

import com.projectcitybuild.pcbridge.http.pcb.PCBHttp
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildNameSuggester
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildsCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildUnvoteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.domain.repositories.BuildRepository
import org.bukkit.plugin.java.JavaPlugin
import org.koin.dsl.module

val buildsModule = module {
    factory {
        BuildsCommand(
            buildListCommand = BuildListCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildCreateCommand = BuildCreateCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
            buildMoveCommand = BuildMoveCommand(
                plugin = get<JavaPlugin>(),
                buildNameSuggester = get(),
                buildRepository = get(),
            ),
            buildVoteCommand = BuildVoteCommand(
                plugin = get<JavaPlugin>(),
                buildNameSuggester = get(),
                buildRepository = get(),
            ),
            buildUnvoteCommand = BuildUnvoteCommand(
                plugin = get<JavaPlugin>(),
                buildNameSuggester = get(),
                buildRepository = get(),
            ),
            buildDeleteCommand = BuildDeleteCommand(
                plugin = get<JavaPlugin>(),
                buildNameSuggester = get(),
                buildRepository = get(),
            ),
            buildEditCommand = BuildEditCommand(
                plugin = get<JavaPlugin>(),
                buildNameSuggester = get(),
                buildRepository = get(),
            ),
            buildSetCommand = BuildSetCommand(
                plugin = get<JavaPlugin>(),
                buildRepository = get(),
            ),
        )
    }

    factory {
        BuildCommand(
            plugin = get<JavaPlugin>(),
            buildNameSuggester = get(),
            buildRepository = get(),
            server = get(),
            playerTeleporter = get(),
        )
    }

    single {
        BuildRepository(
            buildHttpService = get<PCBHttp>().builds
        )
    }

    factory {
        BuildNameSuggester(
            buildRepository = get(),
        )
    }
}