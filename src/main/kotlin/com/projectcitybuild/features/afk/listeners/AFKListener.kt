package com.projectcitybuild.features.afk.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.afk.repositories.AFKRepository
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.Plugin

class AFKListener(
    private val plugin: Plugin,
    private val afkRepository: AFKRepository,
): SpigotListener {

    @EventHandler(priority = EventPriority.LOW)
    fun onBlockBreak(event: BlockBreakEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onBlockPlace(event: BlockPlaceEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onUseCommand(event: PlayerCommandPreprocessEvent) {
        if (event.message.startsWith("/afk")) return

        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            toggleOffAFKIfNeeded(event.damager as Player)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerMove(event: PlayerMoveEvent) {
        // TODO: throttle calls
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onChat(event: AsyncPlayerChatEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        toggleOffAFKIfNeeded(event.player, broadcastToPlayers = false)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        toggleOffAFKIfNeeded(event.player, broadcastToPlayers = false)
    }

    private fun toggleOffAFKIfNeeded(player: Player, broadcastToPlayers: Boolean = true) {
        if (!afkRepository.isAFK(player.uniqueId)) return

        afkRepository.remove(player.uniqueId)

        if (broadcastToPlayers) {
            MessageToBungeecord(
                plugin,
                player,
                SubChannel.AFK_END,
                arrayOf(broadcastToPlayers)
            ).send()
        }
    }
}