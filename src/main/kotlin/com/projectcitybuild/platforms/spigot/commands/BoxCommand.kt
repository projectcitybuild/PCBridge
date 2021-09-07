package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import com.projectcitybuild.platforms.spigot.environment.send
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class BoxCommand(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider
): Commandable {

    override val label = "box"
    override val permission = "pcbridge.box.redeem"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.hasArguments && input.args.first() == "redeem") {
            redeemAvailableBoxes(input)
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
                    val hours = data.secondsUntilRedeemable / 60 / 60
                    if (hours > 0) {
                        "${ChatColor.GRAY}You can redeem more boxes $hours hours"
                    } else {
                        val minutes = data.secondsUntilRedeemable / 60
                        val seconds = data.secondsUntilRedeemable % 60
                        "${ChatColor.GRAY}You can redeem more boxes in $minutes minutes, $seconds seconds"
                    }
                }
                else if (data.redeemableBoxes == null) {
                    "${ChatColor.GRAY}An internal error occurred. Please contact staff"
                }
                else {
                    val totalRedeemableBoxes = data.redeemableBoxes
                        .map { it.quantity }
                        .reduce { total, quantity -> total + quantity }

                    if (totalRedeemableBoxes == 1) {
                        "${ChatColor.GREEN}You have 1 box that can be redeemed! Use ${ChatColor.BOLD}/box redeem"
                    } else {
                        "${ChatColor.GREEN}You have $totalRedeemableBoxes boxes that can be redeemed! Use ${ChatColor.BOLD}/box redeem"
                    }
                }
            }
        }
        player.sendMessage(message)
    }

    private suspend fun redeemAvailableBoxes(input: CommandInput) {
        val player = input.sender as Player

        val donorAPI = apiRequestFactory.pcb.donorApi
        val response = apiClient.execute { donorAPI.redeemAvailableBoxes(player.uniqueId.toString()) }

        when (response) {
            is APIResult.HTTPError -> {
                player.send().error(
                    when (response.error?.id) {
                        "player_not_found" -> "You have not linked your account. Please run /sync first"
                        "player_not_linked" -> "}You have not linked your account. Please run /sync first"
                        "no_donor_perks" -> "You do not have any boxes that can be redeemed"
                        "no_redeemable_boxes" -> "You do not have any boxes that can be redeemed"
                        else -> "No boxes to be redeemed"
                    }
                )
            }
            is APIResult.NetworkError ->
                player.send().error("Failed to contact auth server. Please try again later")

            is APIResult.Success -> {
                val data = response.value.data
                if (data == null) {
                    player.send().error("Data fetch failed. Please contact staff")
                }
                else if (data.secondsUntilRedeemable != null) {
                    val hours = data.secondsUntilRedeemable / 60

                    player.send().error(
                        if (hours > 0) {
                            val seconds = data.secondsUntilRedeemable % 60
                            "${ChatColor.GRAY}You cannot redeem boxes for another $hours hours, $seconds seconds"
                        } else {
                            "${ChatColor.GRAY}You cannot redeem boxes for another ${data.secondsUntilRedeemable} seconds"
                        }
                    )
                }
                else if (data.redeemedBoxes == null) {
                    player.send().error("An internal error occurred. Please contact staff")
                }
                else {
                    val totalRedeemableBoxes = data.redeemedBoxes.sumOf { it.quantity }

                    data.redeemedBoxes.forEach { box ->
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "gmysteryboxes give ${player.name} ${box.quantity}"
                        )
                        logger.info("${player.name} redeemed {$box.quantity} ${box.name} boxes")
                    }

                    player.send().success(
                        if (totalRedeemableBoxes == 1) {
                            "1 box redeemed"
                        } else {
                            "$totalRedeemableBoxes boxes redeemed"
                        }
                    )
                }
            }
        }
    }
}