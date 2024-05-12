package com.projectcitybuild.pcbridge.features.sync.commands

import com.projectcitybuild.pcbridge.features.sync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.pcbridge.features.sync.actions.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Success
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SyncCommand(
    private val generateAccountVerificationURL: GenerateAccountVerificationURL,
    private val updatePlayerGroups: UpdatePlayerGroups,
): SpigotCommand<SyncCommand.Args> {
    override val label = "sync"

    override val usage = CommandHelpBuilder()

    override suspend fun run(sender: CommandSender, args: Args) {
        check (sender is Player) {
            "Only players can use this command"
        }
        when (args.finishSyncing) {
            false -> generateVerificationURL(sender)
            true -> syncGroups(sender)
        }
    }

    private suspend fun generateVerificationURL(player: Player) {
        val result = generateAccountVerificationURL.generate(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                GenerateAccountVerificationURL.FailureReason.ALREADY_LINKED
                -> syncGroups(player)

                GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE
                -> player.sendMessage(
                    Component.text("Failed to generate verification URL: No URL received from server")
                        .color(NamedTextColor.RED)
                )
            }
            is Success -> player.sendMessage(
                Component.text()
                    .append(
                        Component.text("To link your account, please ")
                    )
                    .append(
                        Component.text("click here")
                            .decorate(TextDecoration.UNDERLINED)
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.openUrl(result.value.urlString))
                            .hoverEvent(HoverEvent.showText(Component.text(result.value.urlString)))
                    )
                    .append(
                        Component.text(" and login if required")
                    )
            )
        }
    }

    private suspend fun syncGroups(player: Player) {
        val result = updatePlayerGroups.execute(player.uniqueId)

        val message = when (result) {
            is Failure -> when (result.reason) {
                UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
                -> Component.text("Sync failed. Did you finish registering your account?")
                    .color(NamedTextColor.RED)
            }
            is Success -> {
                Component.text("Account linked! Your rank will be automatically synchronized with the PCB network")
                    .color(NamedTextColor.GREEN)
            }
        }
        player.sendMessage(message)
    }

    data class Args(
        val finishSyncing: Boolean,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    return Args(finishSyncing = false)
                }
                if (args[0] != "finish") {
                    throw BadCommandUsageException()
                }
                return Args(finishSyncing = true)
            }
        }
    }
}
