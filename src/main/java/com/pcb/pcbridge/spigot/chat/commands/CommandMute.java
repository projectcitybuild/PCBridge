package com.pcb.pcbridge.spigot.chat.commands;

import com.pcb.pcbridge.framework.commands.AbstractCommand;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;

public class CommandMute extends AbstractCommand {

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getPermissionNode() {
        return "pcbridge.chat.mute";
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args.length > 2) {
            return false;
        }

        // allow only 'on' or 'off' as a second argument
        Boolean newMuteValue = null;
        if(args.length == 2) {
           String[] options = { "on", "off" };
           boolean isValidOption = Arrays.asList(options).contains(args[1].toLowerCase());

           if(!isValidOption) {
               return false;
           }
           newMuteValue = args[1].equalsIgnoreCase("on");
        }

        // check that the given player name is online
        Player targetPlayer = null;
        for(Player onlinePlayer: sender.getServer().getOnlinePlayers()) {
            if(onlinePlayer.getName().equalsIgnoreCase(args[0])) {
                targetPlayer = onlinePlayer;
                break;
            }
        }

        if(targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found");
            return true;
        }

        PlayerModel player = new PlayerModel(targetPlayer.getUniqueId());
        if(newMuteValue != null) {
            player.setIsMuted(newMuteValue);
        } else {
            // toggle the current value if
            // 'on/off' was not provided
            player.load();
            player.setIsMuted(!player.getIsMuted());
        }

        try {
            player.save();
        } catch(IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to write to file. Please contact an admin");
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + args[0] + " has been muted");

        return true;
    }
}
