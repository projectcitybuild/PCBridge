package com.pcb.pcbridge.spigot.chat.listeners;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.players.PlayerCache;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import com.pcb.pcbridge.framework.listeners.EventListener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public final class ListenerChatMute extends EventListener {

    private PlayerCache playerCache;

    @Inject
    public ListenerChatMute(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        Optional<PlayerModel> cachedPlayer = playerCache.get(player.getUniqueId());
        if(!cachedPlayer.isPresent()) {
            return;
        }

        PlayerModel playerModel = cachedPlayer.get();
        if(playerModel.getIsMuted()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot chat while muted.");
        }
    }
}