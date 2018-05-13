package com.pcb.pcbridge.spigot.maintenance.commands;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.config.ConfigManager;
import com.pcb.pcbridge.features.config.models.ConfigModel;
import com.pcb.pcbridge.framework.commands.AbstractCommand;
import com.pcb.pcbridge.framework.helpers.StringHelpers;
import com.pcb.pcbridge.spigot.maintenance.MaintenanceReminderService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginLogger;

import java.util.Arrays;
import java.util.Optional;

public class CommandMaintenance extends AbstractCommand {

    @Override
    public String getName() {
        return "maintenance";
    }

    @Override
    public String getPermissionNode() {
        return "pcbridge.maintenance.set";
    }


    private ConfigManager configManager;
    private PluginLogger logger;
    private MaintenanceReminderService reminderService;

    @Inject
    public CommandMaintenance(ConfigManager configManager,
                              PluginLogger logger,
                              MaintenanceReminderService reminderService
    ) {
        this.configManager = configManager;
        this.logger = logger;
        this.reminderService = reminderService;
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 1) {
            return false;
        }

        Optional<ConfigModel> optionalConfig = configManager.getConfig();
        if(!optionalConfig.isPresent()) {
            logger.severe("Failed to execute maintenance command: No config present");
            sender.sendMessage(ChatColor.GRAY + "An internal error occurred");
            return true;
        }
        ConfigModel config = optionalConfig.get();

        if(args.length == 1) {
            Optional<Boolean> value = StringHelpers.getBooleanFromString(args[0]);
            value.ifPresent(newValue -> config.isServerMaintenance = newValue);
        } else {
            config.isServerMaintenance = !config.isServerMaintenance;
        }

        configManager.saveConfig(config);

        if(config.isServerMaintenance) {
            reminderService.start(180);
            sender.sendMessage(ChatColor.AQUA + "Server is now in maintenance mode");
        } else {
            reminderService.stop();
            sender.sendMessage(ChatColor.GRAY + "Server maintenance mode deactivated");
        }

        return true;
    }
}
