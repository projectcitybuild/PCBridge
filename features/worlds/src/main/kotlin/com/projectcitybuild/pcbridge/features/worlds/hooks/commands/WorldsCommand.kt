package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command

class WorldsCommand(
    private val worldBorderCommand: WorldBorderCommand,
) : BrigadierCommand {
    override val builder =
        command("worlds") {
            description = "Manages world settings"
            then(worldBorderCommand)
        }
}
