package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.catch
import com.projectcitybuild.pcbridge.paper.architecture.commands.catchSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.homes.commands.HomeNameSuggester
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.runBlocking
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.plugin.Plugin

class HomeRenameCommand(
    private val plugin: Plugin,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("rename")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, homeNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.catchSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { l10n.errorHomeNotFound(name) }

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
                    context.catch {
                        runBlocking {
                            homeRepository.rename(
                                id = home.id,
                                newName = input,
                                player = player,
                            )
                        }
                        player.sendRichMessage(l10n.homeRenamed(input))
                    }
                    return END_OF_CONVERSATION
                }
            })
            .addConversationAbandonedListener { player.sendRichMessage("<gray>Renaming ended</gray>") }
            .buildConversation(player)
            .begin()
    }
}