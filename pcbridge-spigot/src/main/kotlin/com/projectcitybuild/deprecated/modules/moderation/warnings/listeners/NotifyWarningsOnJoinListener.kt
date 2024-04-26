package com.projectcitybuild.modules.moderation.warnings.listeners

import com.projectcitybuild.modules.moderation.warnings.actions.GetUnacknowledgedWarnings
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class NotifyWarningsOnJoinListener(
    private val getUnacknowledgedWarnings: GetUnacknowledgedWarnings,
) : SpigotListener<PlayerJoinEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerJoinEvent) {
        val player = event.player

        CoroutineScope(Dispatchers.IO).launch {
            val warnings = getUnacknowledgedWarnings.execute(
                playerUUID = player.uniqueId,
                playerName = player.name,
            )
            if (warnings.isEmpty()) {
                return@launch
            }
            val tc = TextComponent()
                .add("You have ") { it.color = ChatColor.RED }
                .add(warnings.size) { it.isBold = true }
                .add(" unacknowledged warnings\n") { it.color = ChatColor.RED }
                .add("---\n")

            warnings.forEach { warning ->
                tc.add("${warning.reason}\n")
                tc.add("Date: ") { it.color = ChatColor.GRAY }
                tc.add("${warning.createdAt}\n\n")
                tc.add("[I ACKNOWLEDGE]") {
                    it.isUnderlined = true
                    it.isBold = true
                    it.color = ChatColor.GOLD
                    it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warning acknowledge ${warning.id}")
                }
                tc.add("\n")
                tc.add("---\n")
            }

            tc.add("Click the 'acknowledge' button to mark it as read and hide it") {
                it.color = ChatColor.GRAY
                it.isItalic = true
            }

            player.spigot().sendMessage(tc)
        }
    }
}
