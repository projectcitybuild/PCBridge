package com.projectcitybuild.modules.ranksync.commands

import com.projectcitybuild.modules.ranksync.actions.GenerateAccountVerificationURL
import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.add
import com.projectcitybuild.support.textcomponent.send
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player

class SyncCommand(
    private val generateAccountVerificationURL: GenerateAccountVerificationURL,
    private val updatePlayerGroups: UpdatePlayerGroups,
) {
    // TODO: clean up this horrible mess...
    suspend fun execute(commandSender: Player, finishSyncing: Boolean) {
        when (finishSyncing) {
            false -> generateVerificationURL(commandSender)
            true -> syncGroups(commandSender)
        }
    }

    private suspend fun generateVerificationURL(player: Player) {
        val result = generateAccountVerificationURL.generate(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                GenerateAccountVerificationURL.FailureReason.ALREADY_LINKED
                -> syncGroups(player)

                GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE
                -> player.send().error("Failed to generate verification URL: No URL received from server")
            }
            is Success -> player.spigot().sendMessage(
                TextComponent()
                    .add("To link your account, please ")
                    .add("click here") {
                        it.isBold = true
                        it.isUnderlined = true
                        it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, result.value.urlString)
                        it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(result.value.urlString))
                    }
                    .add(" and login if required")
            )
        }
    }

    private suspend fun syncGroups(player: Player) {
        val result = updatePlayerGroups.execute(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
                -> player.send().error("Sync failed. Did you finish registering your account?")
            }
            is Success -> {
                player.send().success("Account linked! Your rank will be automatically synchronized with the PCB network")
            }
        }
    }
}
