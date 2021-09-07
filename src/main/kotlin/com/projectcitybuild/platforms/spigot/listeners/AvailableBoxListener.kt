package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import org.bukkit.ChatColor
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
                val data = response.value.data
                if (data == null) return

                if (data.redeemableBoxes == null) return

                val redeemableBoxes = data.redeemableBoxes.sumOf { it.quantity }

                val message = if (redeemableBoxes == 1) {
                    "${ChatColor.GRAY}You have ${ChatColor.GREEN}1${ChatColor.GRAY} box that can be redeemed today. Use ${ChatColor.BOLD}/box redeem"
                } else {
                    "${ChatColor.GRAY}You have ${ChatColor.GREEN}$redeemableBoxes${ChatColor.GRAY} boxes that can be redeemed today. Use ${ChatColor.BOLD}/box redeem"
                }
                event.player.sendMessage(message)
            }
        }
    }
}