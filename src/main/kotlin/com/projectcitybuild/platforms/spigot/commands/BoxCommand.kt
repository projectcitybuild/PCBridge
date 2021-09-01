package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class BoxCommand(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Commandable {

    override val label = "box"
    override val permission = "pcbridge.box.redeem"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.hasArguments && input.args.first() == "redeem") {
//            redeemAvailableBoxes(input)
        } else {
            getAvailableBoxes(input)
        }
        return CommandResult.EXECUTED
    }

    private suspend fun getAvailableBoxes(input: CommandInput) {
        val player = input.sender as Player

        val donorAPI = apiRequestFactory.pcb.donorApi
        val response = apiClient.execute { donorAPI.getAvailableBoxes(player.uniqueId.toString()) }

        val message = when (response) {
            is APIResult.HTTPError -> {
                when (response.error?.id) {
                    "player_not_found" -> "${ChatColor.GRAY}You have not linked your account. Please run /sync first"
                    "player_not_linked" -> "${ChatColor.GRAY}You have not linked your account. Please run /sync first"
                    "no_donor_perks" -> "${ChatColor.GRAY}You do not have any boxes that can be redeemed"
                    "no_redeemable_boxes" -> "${ChatColor.GRAY}You do not have any boxes that can be redeemed"
                    else -> "${ChatColor.GRAY}No boxes to be redeemed"
                }
            }
            is APIResult.NetworkError -> "Error: Failed to contact server"
            is APIResult.Success -> {
                val data = response.value.data
                if (data == null) {
                    "${ChatColor.GRAY}Data fetch failed. Please contact staff"
                }
                else if (data.secondsUntilRedeemable != null) {
                    "${ChatColor.GRAY}You can redeem more boxes in ${data.secondsUntilRedeemable} seconds"
                }
                else if (data.redeemableBoxes == null) {
                    "${ChatColor.GRAY}An internal error occurred. Please contact staff"
                }
                else {
                    val totalRedeemableBoxes = data.redeemableBoxes
                        .map { it.quantity ?: 0 }
                        .reduce { total, quantity -> total + quantity }

                    "${ChatColor.GREEN}You have $totalRedeemableBoxes boxes that can be redeemed! Use /box redeem"
                }
            }
        }
        player.sendMessage(message)
    }
//
//    private fun redeemAvailableBoxes(input: CommandInput) {
//
//    }
}