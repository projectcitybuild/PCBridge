package com.pcb.pcbridge.features.config;

import com.google.inject.Inject;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.features.config.models.ConfigModel;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginLogger;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class ConfigManager {

    private WeakReference<PCBridge> _plugin;
    private PluginLogger logger;

    @Inject
    public ConfigManager(PCBridge plugin, PluginLogger logger) {
        this._plugin = new WeakReference<>(plugin);
        this.logger = logger;
    }

    private Optional<PCBridge> getPlugin() {
        return Optional.ofNullable(_plugin.get());
    }

    public void load() {
        getPlugin().ifPresent(plugin -> {
            logger.info("Attempting to load plugin config...");

            Configuration config = plugin.getConfig();
            config.addDefault("server.maintenance", false);
            config.addDefault("database.host", "192.184.93.126");
            config.addDefault("database.port", 3306);
            config.addDefault("database.user", "root");
            config.addDefault("database.pass", "");

            config.options().copyDefaults(true);
            plugin.saveConfig();

            logger.info("Plugin config loaded");
        });
    }

    public Optional<ConfigModel> getConfig() {
        if(!getPlugin().isPresent()) {
            logger.severe("Failed to get plugin config: Missing plugin reference");
            return Optional.empty();
        }
        PCBridge plugin = getPlugin().get();

        Configuration config = plugin.getConfig();

        ConfigModel model = new ConfigModel();
        model.isServerMaintenance   = config.getBoolean("server.maintenance");
        model.databaseHost          = config.getString("database.host");
        model.databasePort          = config.getInt("database.port");
        model.databaseUsername      = config.getString("database.user");
        model.databasePassword      = config.getString("database.pass");

        return Optional.of(model);
    }

    public void saveConfig(ConfigModel model) {
        getPlugin().ifPresent(plugin -> {
            logger.info("Attempting to save plugin config...");

            Configuration config = plugin.getConfig();
            config.set("server.maintenance", model.isServerMaintenance);
            config.set("database.host", model.databaseHost);
            config.set("database.port", model.databasePort);
            config.set("database.user", model.databaseUsername);
            config.set("database.pass", model.databasePassword);

            plugin.saveConfig();

            logger.info("Plugin config saved");
        });
    }

    public void reloadConfig() {
        getPlugin().ifPresent(plugin -> plugin.reloadConfig());
    }

}
