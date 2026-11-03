package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.GameTime
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.GameTimeAlias
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

fun Argument.Companion.gameTime(name: String): CommandArgument<GameTime> = CommandArgument(
    name = name,
    type = GameTimeArgumentParser(),
    valueClass = GameTime::class.java,
)

class GameTimeArgumentParser : CustomArgumentType.Converted<GameTime, String> {
    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun convert(nativeType: String) = GameTime(nativeType)

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val suggestions = buildList {
            addAll(GameTimeAlias.entries.map { it.name.lowercase() })
            add("1000")
            add("+1000")
            add("-1000")
            add("12h")
            add("+12h")
            add("-12h")
        }
        suggestions
            .filter { it.lowercase().startsWith(builder.remaining.lowercase()) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}
