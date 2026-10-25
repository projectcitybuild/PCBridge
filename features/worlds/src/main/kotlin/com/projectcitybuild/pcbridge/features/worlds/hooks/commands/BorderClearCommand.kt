package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.Command
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer

class BorderClearCommand(
    private val worldBorderRepository: WorldBorderRepository,
) : BrigadierCommand {
    override val builder = command("clear") {
        description = l10n.worlds.borderClearCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        executes { context ->
            val player = context.source.requirePlayer()
            worldBorderRepository.clear(player.world)
            player.sendMessage(l10n.worlds.borderCleared)
            Command.SINGLE_SUCCESS
        }
    }
}
