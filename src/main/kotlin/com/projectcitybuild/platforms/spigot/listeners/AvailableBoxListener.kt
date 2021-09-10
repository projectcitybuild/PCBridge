package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import com.projectcitybuild.platforms.spigot.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class AvailableBoxListener(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val donorAPI = apiRequestFactory.pcb.donorApi
        val response = apiClient.execute { donorAPI.getAvailableBoxes(event.player.uniqueId.toString()) }

        when (response) {
            is APIResult.HTTPError -> return
            is APIResult.NetworkError -> return
            is APIResult.Success -> {
                val data = response.value.data ?: return

                if (data.redeemableBoxes == null) return

                val redeemableBoxes = data.redeemableBoxes.sumOf { it.quantity }

                event.player.spigot().sendMessage(
                    TextComponent()
                        .add("You have ") { it.color = ChatColor.GRAY }
                        .add(redeemableBoxes) { it.color = ChatColor.GREEN }
                        .add(" " + if (redeemableBoxes == 1) "box" else "boxes") { it.color = ChatColor.GRAY }
                        .add(" that can be redeemed today. Use ") { it.color = ChatColor.GRAY }
                        .add( "/box redeem") {
                            it.isBold = true
                            it.color = ChatColor.GREEN
                            it.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/box redeem")
                            it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to type this command")))
                        }
                )
            }
        }
    }
}