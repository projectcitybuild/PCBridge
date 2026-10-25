package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command

class WorldsCommand(
    private val worldBorderCommand: WorldBorderCommand,
    private val borderClearCommand: BorderClearCommand,
) : BrigadierCommand {
    override val builder =
        command("worlds") {
            description = l10n.worlds.worldsCommandDescription
            then(worldBorderCommand)
            then(borderClearCommand)
        }
}
