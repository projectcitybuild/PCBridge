package com.pcb.pcbridge.features.players;

import com.google.inject.Singleton;
import com.pcb.pcbridge.features.players.models.PlayerModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PlayerCache {

    private Map<UUID, PlayerModel> players = new HashMap<>();

    public void put(UUID uuid, PlayerModel player) {
        players.put(uuid, player);
    }

    public Optional<PlayerModel> get(UUID uuid) {
        PlayerModel player = players.get(uuid);
        Optional<PlayerModel> result = Optional.ofNullable(player);

        return result;
    }

    public boolean has(UUID uuid) {
        return players.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    public void clear() {
        players.clear();
    }

}
