package com.pcb.pcbridge.spigot.chat.listeners;

import com.pcb.pcbridge.framework.listeners.EventListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ListenerChatMute extends EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(AsyncPlayerChatEvent event) {
//        if (config.IsMuted) {
//            event.setCancelled(true);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while muted.");
//        }
    }
}