package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.AvailableLootBoxes
import com.projectcitybuild.core.network.APIRequestFactory
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class BoxCommand(
        private val scheduler: SchedulerProvider,
        private val apiRequestFactory: APIRequestFactory
): Commandable {

    override val label = "box"
    override val permission = "pcbridge.box.redeem"

    override fun execute(input: CommandInput): CommandResult {
        if (input.hasArguments && input.args.first() == "redeem") {
            redeemAvailableBoxes(input)
        } else {
            getAvailableBoxes(input)
        }
        return CommandResult.EXECUTED
    }

    private fun getAvailableBoxes(input: CommandInput) {
        val player = input.sender as Player

        fetchAvailableBoxes(player.uniqueId) { response ->
            if (response?.error != null) {
                when (response.error.id) {
                    "player_not_found" -> scheduler.sync { player.sendMessage("${ChatColor.GRAY}You have not linked your account. Please run /sync first") }
                    "player_not_linked" -> scheduler.sync { player.sendMessage("${ChatColor.GRAY}You have not linked your account. Please run /sync first") }
                    "no_donor_perks" -> scheduler.sync { player.sendMessage("${ChatColor.GRAY}You do not have any boxes that can be redeemed") }
                    "no_redeemable_boxes" -> scheduler.sync { player.sendMessage("${ChatColor.GRAY}You do not have any boxes that can be redeemed") }
                    else -> scheduler.sync { player.sendMessage("${ChatColor.GRAY}No boxes to be redeemed") }
                }
                return@fetchAvailableBoxes
            }
            if (response?.data == null) {
                scheduler.sync { player.sendMessage("${ChatColor.GRAY}Could not connect to server") }
                return@fetchAvailableBoxes
            }
            if (response.data.secondsUntilRedeemable != null) {
                scheduler.sync { player.sendMessage("${ChatColor.GRAY}You can redeem more boxes in ${response.data.secondsUntilRedeemable} seconds") }
                return@fetchAvailableBoxes
            }
            if (response.data.redeemableBoxes == null) {
                scheduler.sync { player.sendMessage("${ChatColor.GRAY}An internal error occurred. Please contact staff") }
                return@fetchAvailableBoxes
            }
            val totalRedeemableBoxes = response.data.redeemableBoxes
                .map { it.quantity ?: 0 }
                .reduce { total, quantity -> total + quantity }

            scheduler.sync { player.sendMessage("${ChatColor.GREEN}You have ${totalRedeemableBoxes} boxes that can be redeemed! Use /box redeem") }
        }
    }

    private fun redeemAvailableBoxes(input: CommandInput) {

    }

    private fun fetchAvailableBoxes(uuid: UUID, completion: (ApiResponse<AvailableLootBoxes>?) -> Unit) {
        scheduler.async<ApiResponse<AvailableLootBoxes>?> { resolve ->
            val response = apiRequestFactory.pcb.donorApi.getAvailableBoxes(uuid.toString()).execute()
            val body = response.body()
            resolve(body)
        }.startAndSubscribe(completion)
    }
}