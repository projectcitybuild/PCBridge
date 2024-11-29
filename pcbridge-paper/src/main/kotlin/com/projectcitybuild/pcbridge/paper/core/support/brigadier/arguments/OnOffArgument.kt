package com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class OnOffArgument: CustomArgumentType.Converted<Boolean, String> {
    override fun getNativeType(): ArgumentType<String>
        = StringArgumentType.word()

    override fun convert(nativeType: String): Boolean {
        return when (nativeType.lowercase()) {
            "on" -> true
            "off" -> false
            else -> {
                val message = MessageComponentSerializer.message()
                    .serialize(Component.text("Invalid value: $nativeType", NamedTextColor.RED))

                throw CommandSyntaxException(SimpleCommandExceptionType(message), message)
            }
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = builder
        .apply {
            suggest("on")
            suggest("off")
        }
        .buildFuture()
}