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

class EnumArgument<T : Enum<T>>(
    private val enumClass: Class<T>,
) : CustomArgumentType.Converted<T, String> {
    override fun convert(nativeType: String): T = try {
        enumClass.enumConstants?.firstOrNull { it.name.equals(nativeType, ignoreCase = true) }
            ?: throw IllegalArgumentException("Invalid value: $nativeType")
    } catch (e: IllegalArgumentException) {
        val message =
            MessageComponentSerializer
                .message()
                .serialize(l10n.errors.invalidArgumentValue(nativeType))

        throw CommandSyntaxException(SimpleCommandExceptionType(message), message)
    }

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        enumClass.enumConstants?.forEach { value ->
            builder.suggest(value.name)
        }
        return builder.buildFuture()
    }
}
