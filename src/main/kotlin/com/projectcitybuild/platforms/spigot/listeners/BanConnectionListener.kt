package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.text.SimpleDateFormat
import java.util.*

class BanConnectionListener(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {

        // Events cannot be mutated (i.e. we cannot kick the connecting player) inside a
        // suspended function due to the way the code becomes compiled, so we need to block.
        // Blocking isn't actually a problem here, because the event is already asynchronous
        //
        // See https://github.com/Shynixn/MCCoroutine/issues/43
        runBlocking {
            val action = CheckBanStatusAction(apiRequestFactory, apiClient)
            val result = action.execute(playerId = event.uniqueId)

            when (result) {
                is Failure -> return@runBlocking
                is Success -> {
                    val ban = result.value ?: return@runBlocking

                    val expiryDate = ban.expiresAt?.let {
                        val date = Date(it * 1000)
                        val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        format.format(date)
                    } ?: "Never"

                    val textComponent = TextComponent().apply {
                        this.addExtra(TextComponent("You are currently banned.\n\n").apply {
                            this.color = ChatColor.RED
                            this.isBold = true
                        })
                        this.addExtra(TextComponent("Reason: ").apply {
                            this.color = ChatColor.GRAY
                        })
                        this.addExtra(TextComponent((ban.reason ?: "No reason provided") + "\n").apply {
                            this.color = ChatColor.WHITE
                        })
                        this.addExtra(TextComponent("Expires: ").apply {
                            this.color = ChatColor.GRAY
                        })
                        this.addExtra(TextComponent(expiryDate + "\n\n").apply {
                            this.color = ChatColor.WHITE
                        })
                        this.addExtra(TextComponent("Appeal @ https://projectcitybuild.com").apply {
                            this.color = ChatColor.AQUA
                        })
                    }
                    event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        textComponent.toLegacyText()
                    )
                }
            }
        }
    }
}