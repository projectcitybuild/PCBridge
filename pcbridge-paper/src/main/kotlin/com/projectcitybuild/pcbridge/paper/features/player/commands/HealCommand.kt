package com.projectcitybuild.pcbridge.paper.features.player.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectTypeCategory

class HealCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack>
        = Commands.literal("heal")
            .requiresPermission(PermissionNode.PLAYER_GAMEPLAY)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val sender = context.source.sender
        val player = context.getOptionalArgument("player", Player::class.java)
            ?: context.source.requirePlayer()

        // Remove any negative effects or debuffs
        player.foodLevel = 20 // 20 is max
        player.saturation = 20f
        player.exhaustion = 0f
        player.fireTicks = 0
        player.activePotionEffects
            .filter { it.type.category == PotionEffectTypeCategory.HARMFUL }
            .forEach { player.removePotionEffect(it.type) }

        player.heal(player.getAttribute(Attribute.MAX_HEALTH)!!.defaultValue)
        player.sendRichMessage(l10n.youHaveBeenHealed)

        if (player != sender) {
            sender.sendRichMessage(l10n.healedPlayer(player.name))
        }
    }
}
