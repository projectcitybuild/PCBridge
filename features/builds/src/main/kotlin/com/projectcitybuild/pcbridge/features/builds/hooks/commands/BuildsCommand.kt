package com.projectcitybuild.pcbridge.features.builds.hooks.commands

import com.projectcitybuild.pcbridge.features.builds.BuildsPermissions
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildUnvoteCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.command

class BuildsCommand(
    private val buildCreateCommand: BuildCreateCommand,
    private val buildDeleteCommand: BuildDeleteCommand,
    private val buildEditCommand: BuildEditCommand,
    private val buildListCommand: BuildListCommand,
    private val buildMoveCommand: BuildMoveCommand,
    private val buildSetCommand: BuildSetCommand,
    private val buildUnvoteCommand: BuildUnvoteCommand,
    private val buildVoteCommand: BuildVoteCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("builds") {
        then(buildCreateCommand)
        then(buildDeleteCommand)
        then(buildEditCommand)
        then(buildListCommand)
        then(buildMoveCommand)
        then(buildSetCommand)
        then(buildUnvoteCommand)
        then(buildVoteCommand)
        // TODO: can we use a Redirect here? Would be good to allow a page arg
        requiresPermission(BuildsPermissions.TELEPORT)
        executes(buildListCommand.literal().command)
    }
}
