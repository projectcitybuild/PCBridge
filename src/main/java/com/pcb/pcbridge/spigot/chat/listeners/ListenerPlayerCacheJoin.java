package com.pcb.pcbridge.spigot.chat.listeners;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.players.PlayerCache;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import com.pcb.pcbridge.framework.listeners.EventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public final class ListenerPlayerCacheJoin extends EventListener {

    private PlayerCache playerCache;

    @Inject
    public ListenerPlayerCacheJoin(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerModel playerModel = new PlayerModel(player.getUniqueId());
        playerModel.load();

        playerCache.put(player.getUniqueId(), playerModel);
    }

}