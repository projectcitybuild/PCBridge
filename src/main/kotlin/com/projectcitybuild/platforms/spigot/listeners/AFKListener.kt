package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
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
    private val plugin: Plugin
): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        toggleOffAFKIfNeeded(event.player, true)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        toggleOffAFKIfNeeded(event.player, true)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        toggleOffAFKIfNeeded(event.player, true)
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (!event.message.startsWith("/afk")) {
            toggleOffAFKIfNeeded(event.player, true)
        }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            toggleOffAFKIfNeeded(event.damager as Player, true)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        toggleOffAFKIfNeeded(event.player, true)
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        toggleOffAFKIfNeeded(event.player, true)
    }

    @EventHandler
    fun onQuit(event: AsyncPlayerChatEvent) {
        toggleOffAFKIfNeeded(event.player, false)
    }

    private fun toggleOffAFKIfNeeded(player: Player, broadcastToPlayers: Boolean) {
        MessageToBungeecord(plugin, player, SubChannel.AFK_END, broadcastToPlayers)
            .send()
    }
}