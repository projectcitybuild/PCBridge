package com.pcb.pcbridge.spigot.maintenance.listeners;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.config.ConfigManager;
import com.pcb.pcbridge.features.config.models.ConfigModel;
import com.pcb.pcbridge.framework.Colors;
import com.pcb.pcbridge.framework.RawColor;
import com.pcb.pcbridge.framework.listeners.EventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginLogger;

import java.util.Optional;

public final class ListenerMaintenanceLogin extends EventListener {

    private ConfigManager configManager;
    private PluginLogger logger;

    @Inject
    public ListenerMaintenanceLogin(ConfigManager configManager, PluginLogger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * Kick anyone trying to enter if the
     * server is in maintenance mode
     *
     * @param event
     */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handle(PlayerLoginEvent event) {
        Optional<ConfigModel> optionalConfig = configManager.getConfig();
        if(!optionalConfig.isPresent()) {
            logger.severe("Failed to handle maintenance login: No config present");
            return;
        }

        ConfigModel config = optionalConfig.get();
        if(!config.isServerMaintenance || event.getPlayer().hasPermission("pcbridge.maintenance.access")) {
            return;
        }

        String message = RawColor.AQUA + "Server maintenance in progress\n\n" +
                         RawColor.WHITE + "Visit projectcitybuild.com for details";

        event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, message);
	}
}
