package com.projectcitybuild.features.afk.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
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
): SpigotListener {

//    @EventHandler(priority = EventPriority.LOW)
//    fun onBlockBreak(event: BlockBreakEvent) {
//        toggleOffAFKIfNeeded(event.player, true)
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onBlockPlace(event: BlockPlaceEvent) {
//        toggleOffAFKIfNeeded(event.player, true)
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onPlayerInteract(event: PlayerInteractEvent) {
//        toggleOffAFKIfNeeded(event.player, true)
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onCommand(event: PlayerCommandPreprocessEvent) {
//        if (!event.message.startsWith("/afk")) {
//            toggleOffAFKIfNeeded(event.player, true)
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onAttack(event: EntityDamageByEntityEvent) {
//        if (event.damager is Player) {
//            toggleOffAFKIfNeeded(event.entity as Player, true)
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onMove(event: PlayerMoveEvent) {
//        toggleOffAFKIfNeeded(event.player, true)
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onChat(event: AsyncPlayerChatEvent) {
//        toggleOffAFKIfNeeded(event.player, true)
//    }
//
//    @EventHandler(priority = EventPriority.LOW)
//    fun onQuit(event: AsyncPlayerChatEvent) {
//        toggleOffAFKIfNeeded(event.player, false)
//    }
//
//    private fun toggleOffAFKIfNeeded(player: Player, broadcastToPlayers: Boolean) {
//        MessageToBungeecord(plugin, player, SubChannel.AFK_END, arrayOf(broadcastToPlayers))
//            .send()
//    }
}