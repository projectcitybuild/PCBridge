package com.pcb.pcbridge.spigot.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pcb.pcbridge.PCBridge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Singleton
public class MaintenanceReminderService {

    private PCBridge plugin;

    @Inject
    public MaintenanceReminderService(PCBridge plugin) {
        this.plugin = plugin;
    }

    private Integer taskId;

    /**
     * Starts a task that messages all online
     * users that the server is in maintenance
     * mode, every X seconds
     */
    public void start(int intervalInSeconds) {
        if(taskId != null) {
            return;
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendMessage(ChatColor.YELLOW + "Notice: Server is currently in maintenance mode...");
            }
        }, 20 * intervalInSeconds, 20 * intervalInSeconds);
    }

    /**
     * Stops the message task if it's
     * currently running
     */
    public void stop() {
        if(taskId == null) {
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
