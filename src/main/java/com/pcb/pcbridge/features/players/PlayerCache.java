package com.pcb.pcbridge.features.players;

import com.google.inject.Singleton;
import com.pcb.pcbridge.features.players.models.PlayerModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public PlayerModel get(UUID uuid, Supplier<PlayerModel> orMake) {
        Optional<PlayerModel> player = get(uuid);
        if(player.isPresent()) {
            return player.get();
        }

        PlayerModel newValue = orMake.get();
        players.put(uuid, newValue);

        return newValue;
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
