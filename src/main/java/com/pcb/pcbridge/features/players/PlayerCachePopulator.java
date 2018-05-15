package com.pcb.pcbridge.features.players;

import com.google.inject.Inject;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerCachePopulator {

    private PlayerCache cache;

    @Inject
    public PlayerCachePopulator(PlayerCache cache) {
        this.cache = cache;
    }

    public void populate(PCBridge plugin) {
        cache.clear();
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if(cache.has(uuid)) {
            return;
        }

        PlayerModel model = new PlayerModel(uuid);
        cache.put(uuid, model);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        cache.remove(uuid);
    }

}
