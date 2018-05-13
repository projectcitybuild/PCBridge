package com.pcb.pcbridge.spigot.maintenance.listeners;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.config.ConfigManager;
import com.pcb.pcbridge.features.config.models.ConfigModel;
import com.pcb.pcbridge.framework.RawColor;
import com.pcb.pcbridge.framework.listeners.EventListener;
import com.pcb.pcbridge.spigot.maintenance.MaintenanceReminderService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginLogger;

import java.util.Optional;

public final class ListenerMaintenanceLogin extends EventListener {

    private ConfigManager configManager;
    private PluginLogger logger;
    private MaintenanceReminderService reminderService;

    @Inject
    public ListenerMaintenanceLogin(ConfigManager configManager,
                                    PluginLogger logger,
                                    MaintenanceReminderService reminderService
    ) {
        this.configManager = configManager;
        this.logger = logger;
        this.reminderService = reminderService;
    }

    /**
     * Starts the maintenance reminder service
     * if the server is already in maintenance mode
     */
    @Override
    public void onRegister() {
        Optional<ConfigModel> optionalConfig = configManager.getConfig();
        if(!optionalConfig.isPresent()) {
            logger.severe("Failed to check if maintenance mode: No config present");
            return;
        }

        ConfigModel config = optionalConfig.get();
        if(config.isServerMaintenance) {
            reminderService.start(180);
        }
    }

    /**
     * Stops the maintenance reminder service
     * before the plugin is disabled
     */
    @Override
    public void onDeregister() {
        reminderService.stop();
    }

    /**
     * Kicks anyone trying to enter if the
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
