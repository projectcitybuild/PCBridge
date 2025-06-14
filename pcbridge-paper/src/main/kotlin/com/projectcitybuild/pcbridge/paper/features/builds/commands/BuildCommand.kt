package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.http.pcb.models.Build
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class BuildCommand(
    private val plugin: Plugin,
    private val buildNameSuggester: BuildNameSuggester,
    private val buildRepository: BuildRepository,
    private val server: Server,
    private val playerTeleporter: PlayerTeleporter,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("build")
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, buildNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val build = buildRepository.get(name = name)
        checkNotNull(build) { "Build not found" }

        val world = server.getWorld(build.world)
        checkNotNull(world) { l10n.errorWorldNotFound(build.world) }

        playerTeleporter.move(
            player,
            destination = build.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            options = PlayerTeleporter.TeleportOptions(
                preloadDestinationChunk = true,
            ),
        )
        val owner = build.player?.alias

        player.showTitle(
            Title.title(
                Component.text(build.name),
                MiniMessage.miniMessage().deserialize(
                    if (owner.isNullOrEmpty()) ""
                    else "<gray>Created by $owner</gray>"
                ),
            )
        )
        context.source.sender.sendRichMessage(
            "<gray>Teleported to <aqua>$name</aqua></gray>",
        )
        if (!build.description.isNullOrEmpty()) {
            player.sendRichMessage("<gray>---<newline>${build.description}</gray>")
        }
        if (!build.lore.isNullOrEmpty()) {
            player.sendRichMessage("<gray>---<newline><italic>${build.lore}</italic></gray>")
        }
    }
}

// TODO: reuse
private fun Build.toLocation(world: World)
    = Location(world, x, y, z, yaw, pitch)