package com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.core.l10n.l10n
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

class OnOffArgument : CustomArgumentType.Converted<Boolean, String> {
    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun convert(nativeType: String): Boolean = when (nativeType.lowercase()) {
        "on" -> {
            true
        }
        "off" -> {
            false
        }
        else -> {
            val message =
                MessageComponentSerializer
                    .message()
                    .serialize(l10n.errors.invalidArgumentValue(nativeType))

            throw CommandSyntaxException(SimpleCommandExceptionType(message), message)
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = builder
        .apply {
            suggest("on")
            suggest("off")
        }.buildFuture()
}
