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

fun Argument.Companion.worldSelector(
    name: String,
    server: Server,
): CommandArgument<WorldSelector> = CommandArgument(
    name = name,
    type = WorldSelectorArgumentParser(server),
    valueClass = WorldSelector::class.java,
)

class WorldSelectorArgumentParser(
    private val server: Server,
) : CustomArgumentType.Converted<WorldSelector, String> {
    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun convert(nativeType: String): WorldSelector {
        if (nativeType.isEmpty()) {
            return WorldSelector.Current
        }
        if (nativeType == "@all") {
            return WorldSelector.All
        }
        return WorldSelector.Named(nativeType)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        builder.suggest("@all")

        for (world in server.worlds) {
            builder.suggest(world.name)
        }
        return builder.buildFuture()
    }
}
