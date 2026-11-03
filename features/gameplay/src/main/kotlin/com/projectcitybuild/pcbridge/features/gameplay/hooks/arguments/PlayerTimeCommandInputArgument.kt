package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.Server
import java.util.concurrent.CompletableFuture

data class PlayerTimeCommandInput(
    val time: String,
    val playerName: String?,
)

fun Argument.Companion.playerTimeInput(
    name: String,
    server: Server,
): CommandArgument<PlayerTimeCommandInput> = CommandArgument(
    name = name,
    type = PlayerTimeCommandInputArgumentParser(server),
    valueClass = PlayerTimeCommandInput::class.java,
)

class PlayerTimeCommandInputArgumentParser(
    private val server: Server,
) : CustomArgumentType.Converted<PlayerTimeCommandInput, String> {
    private val timeParser = GameTimeArgumentParser()

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.greedyString()

    override fun convert(nativeType: String): PlayerTimeCommandInput {
        val input = nativeType.trim()
        val separator = input.lastIndexOf(' ')
        return if (separator < 0) {
            PlayerTimeCommandInput(input, null)
        } else {
            PlayerTimeCommandInput(
                time = input.substring(0, separator),
                playerName = input.substring(separator + 1),
            )
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val separator = builder.remaining.lastIndexOf(' ')
        if (separator < 0) {
            return timeParser.listSuggestions(context, builder)
        }

        val playerBuilder = builder.createOffset(builder.start + separator + 1)
        server.onlinePlayers
            .filter { it.name.startsWith(playerBuilder.remaining, ignoreCase = true) }
            .forEach { playerBuilder.suggest(it.name) }
        return playerBuilder.buildFuture()
    }
}
