package com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.platform.paper.data.GameTime
import com.projectcitybuild.pcbridge.platform.paper.data.GameTimeAlias
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

class GameTimeArgument(
    private val allowFixedTime: Boolean = false,
    private val suggestionsAfterTime: () -> Collection<String> = { emptyList() },
) : CustomArgumentType.Converted<GameTime, String> {
    override fun getNativeType(): ArgumentType<String> = StringArgumentType.greedyString()

    override fun convert(nativeType: String) = GameTime(nativeType)

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val remaining = builder.remaining
        val hasTrailingArgument = ' ' in remaining
        val prefix = remaining.substringAfterLast(' ').lowercase()
        val suggestionBuilder = if (hasTrailingArgument) {
            builder.createOffset(builder.start + remaining.lastIndexOf(' ') + 1)
        } else {
            builder
        }
        val suggestions = if (hasTrailingArgument) {
            suggestionsAfterTime()
        } else {
            GameTimeAlias.entries.flatMap { alias ->
                if (allowFixedTime) listOf(alias.argument, "@${alias.argument}") else listOf(alias.argument)
            }
        }
        suggestions
            .filter { it.lowercase().startsWith(prefix) }
            .forEach(suggestionBuilder::suggest)
        return suggestionBuilder.buildFuture()
    }
}
