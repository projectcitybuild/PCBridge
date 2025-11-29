package com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.onlinePlayer
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class SingleOnlinePlayerArgument(
    private val server: Server,
): CustomArgumentType.Converted<Player, String> {
    override fun getNativeType(): ArgumentType<String>
        = StringArgumentType.word()

    override fun convert(nativeType: String): Player {
        val player = server.onlinePlayer(name = nativeType, ignoreCase = false)
        if (player == null) {
            val message = MessageComponentSerializer.message()
                .serialize(Component.text("Player $nativeType not found", NamedTextColor.RED))

            throw CommandSyntaxException(SimpleCommandExceptionType(message), message)
        }
        return player
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