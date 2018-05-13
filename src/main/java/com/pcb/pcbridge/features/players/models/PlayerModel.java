package com.pcb.pcbridge.features.players.models;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerModel {

    private UUID uuid;

    private File _storagePath;
    private File getStoragePath() {
        if (_storagePath == null) {
            _storagePath = new File(uuid + ".yml");
        }
        return _storagePath;
    }

    private boolean isMuted;

    public PlayerModel(UUID uuid) {
        this.uuid = uuid;
        this.isMuted = false;
    }

    public void load() {
        YamlConfiguration reader = YamlConfiguration.loadConfiguration(getStoragePath());
        isMuted = reader.getBoolean("chat.muted");
    }

    public void save() throws IOException {
        File storagePath = getStoragePath();

        YamlConfiguration reader = YamlConfiguration.loadConfiguration(storagePath);
        reader.set("chat.muted", isMuted);
        reader.save(storagePath);
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    public boolean getIsMuted() {
        return isMuted;
    }

}
