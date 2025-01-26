package com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.Server
import java.util.concurrent.CompletableFuture

class OnlinePlayerNameArgument(
    private val server: Server,
): CustomArgumentType.Converted<String, String> {
    override fun getNativeType(): ArgumentType<String>
        = StringArgumentType.word()

    override fun convert(nativeType: String): String {
        return server
            .onlinePlayers
            .firstOrNull { it.name == nativeType }
            .let { it?.name }
            ?: nativeType
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        return builder.run {
            server.onlinePlayers.forEach { suggest(it.name) }
            buildFuture()
        }
    }
}