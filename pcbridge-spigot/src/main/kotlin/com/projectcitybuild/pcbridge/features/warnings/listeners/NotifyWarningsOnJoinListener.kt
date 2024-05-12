package com.projectcitybuild.pcbridge.features.warnings.listeners

import com.projectcitybuild.pcbridge.features.warnings.actions.GetUnacknowledgedWarnings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class NotifyWarningsOnJoinListener(
    private val getUnacknowledgedWarnings: GetUnacknowledgedWarnings,
) : Listener {
    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val warnings = getUnacknowledgedWarnings.execute(
            playerUUID = player.uniqueId,
            playerName = player.name,
        )
        if (warnings.isEmpty()) {
            return
        }

        val message = Component.text()
            .append(
                Component.text("You have ").color(NamedTextColor.RED),
                Component.text(warnings.size).decorate(TextDecoration.BOLD),
                Component.text(" unacknowledged warnings").color(NamedTextColor.RED),
                Component.newline(),
                Component.text("---").color(NamedTextColor.GRAY),
                Component.newline(),
            )

        warnings.forEach { warning ->
            message.append(
                Component.text(warning.reason),
                Component.newline(),
                Component.text("Date: ").color(NamedTextColor.GRAY),
                Component.text(warning.createdAt),
                Component.newline(),
                Component.newline(),
                Component.text("[I ACKNOWLEDGE]")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand("/warning acknowledge ${warning.id}")),
                Component.newline(),
                Component.text("---").color(NamedTextColor.GRAY),
                Component.newline(),
            )
        }
        message.append(
            Component.text("Click the 'acknowledge' button to mark it as read and hide it")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC),
        )
        player.sendMessage(message)
    }
}
