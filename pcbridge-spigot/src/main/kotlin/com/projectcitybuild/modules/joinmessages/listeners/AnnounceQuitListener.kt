package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.modules.joinmessages.PlayerJoinTimeCache
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import com.projectcitybuild.support.spigot.SpigotServer
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration

class AnnounceQuitListener(
    private val server: SpigotServer,
    private val playerJoinTimeCache: PlayerJoinTimeCache,
    private val time: Time,
) : SpigotListener<PlayerQuitEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerQuitEvent) {
        val joinTime = playerJoinTimeCache.get(event.player.uniqueId)
        playerJoinTimeCache.remove(event.player.uniqueId)

        server.broadcastMessage(
            TextComponent()
                .add("- ") {
                    it.color = ChatColor.RED
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" left the server") { it.color = ChatColor.GRAY }
                .also {
                    if (joinTime != null) {
                        val now = time.now()
                        val diff = Duration.between(joinTime, now)

                        val secsOnline = diff.toSeconds()
                        val minsOnline = diff.toMinutes()
                        val hoursOnline = diff.toHours()

                        val timeOnline = if (secsOnline < 60) {
                            "$secsOnline sec" + if (secsOnline > 1) "s" else ""
                        } else if(minsOnline < 60) {
                            "$minsOnline min" + if (minsOnline > 1) "s" else ""
                        } else {
                            "$hoursOnline hour" + if (hoursOnline > 1) "s" else ""
                        }

                        it.add(" (online for $timeOnline)")
                    }
                }
        )
    }
}
