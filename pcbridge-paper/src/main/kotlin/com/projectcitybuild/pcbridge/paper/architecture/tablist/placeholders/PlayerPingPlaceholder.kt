package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration.Companion.seconds

class PlayerPingPlaceholder(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val tabRenderer: TabRenderer,
    private val spigotTimer: SpigotTimer,
): UpdatableTabPlaceholder {
    private var cancellable: Cancellable? = null

    override val placeholder: String = "%ping%"

    override suspend fun value(player: Player): Component {
        val ping = player.ping
        val color = when {
            ping <= 100 -> "green"
            ping <= 200 -> "yellow"
            ping <= 400 -> "orange"
            else -> "red"
        }
        return MiniMessage.miniMessage().deserialize("<gray>[<$color>${ping}ms</$color>]</gray>")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin != plugin) {
            // PluginEnableEvent is emitted for every plugin, not just ours
            return
        }
        cancellable?.cancel()
        cancellable = spigotTimer.scheduleRepeating(
            identifier = "tab_player_ping",
            delay = 10.seconds,
            repeatingInterval = 10.seconds,
        ) {
            // TODO: remove runBlocking when SpigotTimer becomes coroutines
            runBlocking {
                server.onlinePlayers.forEach { player ->
                    tabRenderer.updatePlayerName(player)
                }
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin != plugin) {
            // PluginDisableEvent is emitted for every plugin, not just ours
            return
        }
        cancellable?.cancel()
        cancellable = null
    }
}