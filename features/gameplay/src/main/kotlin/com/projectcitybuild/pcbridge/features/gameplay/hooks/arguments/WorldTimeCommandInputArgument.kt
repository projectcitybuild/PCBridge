package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.Server
import java.util.concurrent.CompletableFuture

data class WorldTimeCommandInput(
    val time: String,
    val selector: WorldSelector,
)

fun Argument.Companion.worldTimeInput(
    name: String,
    server: Server,
): CommandArgument<WorldTimeCommandInput> = CommandArgument(
    name = name,
    type = WorldTimeCommandInputArgumentParser(server),
    valueClass = WorldTimeCommandInput::class.java,
)

class WorldTimeCommandInputArgumentParser(
    server: Server,
) : CustomArgumentType.Converted<WorldTimeCommandInput, String> {
    private val worldSelectorParser = WorldSelectorArgumentParser(server)
    private val timeParser = GameTimeArgumentParser()

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.greedyString()

    override fun convert(nativeType: String): WorldTimeCommandInput {
        val input = nativeType.trim()
        val separator = input.lastIndexOf(' ')
        return if (separator < 0) {
            WorldTimeCommandInput(input, WorldSelector.Current)
        } else {
            WorldTimeCommandInput(
                time = input.substring(0, separator),
                selector = worldSelectorParser.convert(input.substring(separator + 1)),
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

        return worldSelectorParser.listSuggestions(
            context,
            builder.createOffset(builder.start + separator + 1),
        )
    }
}
