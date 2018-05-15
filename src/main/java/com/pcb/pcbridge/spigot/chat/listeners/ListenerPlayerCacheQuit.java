package com.pcb.pcbridge.spigot.chat.listeners;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.players.PlayerCache;
import com.pcb.pcbridge.framework.listeners.EventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ListenerPlayerCacheQuit extends EventListener {

    private PlayerCache playerCache;

    @Inject
    public ListenerPlayerCacheQuit(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerCache.remove(player.getUniqueId());
    }

}