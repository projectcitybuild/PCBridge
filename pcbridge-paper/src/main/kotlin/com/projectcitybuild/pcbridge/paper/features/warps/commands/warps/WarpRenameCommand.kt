package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.trace
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.warps.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.runBlocking
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.plugin.Plugin

class WarpRenameCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("rename")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, warpNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val warp = warpRepository.get(name)
        checkNotNull(warp) { l10n.errorWarpNotFound(name) }

        // TODO: this could do with some Kotlin good-ness...
        ConversationFactory(plugin)
            .withModality(true)
            .withLocalEcho(false)
            .withEscapeSequence("cancel")
            .withTimeout(30)
            .withFirstPrompt(object : StringPrompt() {
                override fun getPromptText(context: ConversationContext): String
                    = "Enter a new name (or type 'cancel' to abort):"

                override fun acceptInput(convoContext: ConversationContext, input: String?): Prompt? {
                    if (input == null) {
                        return null
                    }
                    context.trace {
                        runBlocking {
                            warpRepository.rename(
                                id = warp.id,
                                newName = input,
                            )
                        }
                        player.sendRichMessage(l10n.homeRenamed(input))
                    }
                    return Prompt.END_OF_CONVERSATION
                }
            })
            .addConversationAbandonedListener { player.sendRichMessage("<gray>Renaming ended</gray>") }
            .buildConversation(player)
            .begin()
    }
}
