package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.platforms.spigot.SessionCache
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin

class AFKListener(
    private val plugin: Plugin,
    private val sessionCache: SessionCache
): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (!event.message.startsWith("/afk")) {
            toggleOffAFKIfNeeded(event.player)
        }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            toggleOffAFKIfNeeded(event.damager as Player)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        toggleOffAFKIfNeeded(event.player)
    }

    @EventHandler
    fun onQuit(event: AsyncPlayerChatEvent) {
        sessionCache.afkPlayerList.remove(event.player.uniqueId)
    }

    private fun toggleOffAFKIfNeeded(player: Player) {
        if (sessionCache.afkPlayerList.contains(player.uniqueId)) {
            sessionCache.afkPlayerList.remove(player.uniqueId)
            plugin.server.broadcastMessage("${ChatColor.GRAY}${player.displayName} is no longer AFK")
        }
    }
}