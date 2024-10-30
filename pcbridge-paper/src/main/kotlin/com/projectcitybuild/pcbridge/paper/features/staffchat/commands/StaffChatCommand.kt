package com.projectcitybuild.pcbridge.paper.features.staffchat.commands

import com.projectcitybuild.pcbridge.paper.Permissions
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Server
import org.bukkit.command.CommandSender

class StaffChatCommand(
    private val server: Server,
    private val remoteConfig: RemoteConfig,
) : SpigotCommand<StaffChatCommand.Args> {
    override val label = "a"

    override val usage = CommandHelpBuilder(usage = "/a <message>")

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        // Only the legacy serializer automatically converts URLs to clickable text
        val legacySerializer = LegacyComponentSerializer
            .builder()
            .extractUrls()
            .build()

        val format = remoteConfig.latest.config.chat.staffChannel

        val message = MiniMessage.miniMessage().deserialize(
            format,
            Placeholder.component("name", Component.text(sender.name)),
            Placeholder.component("message", legacySerializer.deserialize(args.message)),
        )

        server.onlinePlayers
            .filter { it.hasPermission(Permissions.COMMAND_STAFF_CHAT) }
            .forEach { it.sendMessage(message) }
    }

    data class Args(
        val message: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(message = args.joinToString(separator = " "))
            }
        }
    }
}
