package com.projectcitybuild.pcbridge.features.homes.hooks.commands

import com.projectcitybuild.pcbridge.features.homes.HomesPermissions
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeRenameCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode

class HomesCommand(
    private val homeCreateCommand: HomeCreateCommand,
    private val homeDeleteCommand: HomeDeleteCommand,
    private val homeListCommand: HomeListCommand,
    private val homeMoveCommand: HomeMoveCommand,
    private val homeLimitCommand: HomeLimitCommand,
    private val homeRenameCommand: HomeRenameCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("homes") {
        requiresPermission(HomesPermissions.USE)
        then(homeCreateCommand)
        then(homeDeleteCommand)
        then(homeListCommand)
        then(homeMoveCommand)
        then(homeLimitCommand)
        then(homeRenameCommand)
        executes(homeListCommand.literal().command)
    }
}
