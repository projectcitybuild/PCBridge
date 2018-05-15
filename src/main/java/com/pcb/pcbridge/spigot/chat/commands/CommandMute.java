package com.pcb.pcbridge.spigot.chat.commands;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.players.PlayerCache;
import com.pcb.pcbridge.framework.commands.AbstractCommand;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import com.pcb.pcbridge.framework.helpers.StringHelpers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandMute extends AbstractCommand {

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getPermissionNode() {
        return "pcbridge.chat.mute";
    }


    private PlayerCache playerCache;

    @Inject
    public CommandMute(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args.length > 2) {
            return false;
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
        if(args.length == 2) {
            Optional<Boolean> value = StringHelpers.getBooleanFromString(args[1]);
            if(!value.isPresent()) {
                return false;
            }
            player.setIsMuted(value.get());
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

        // update player cache
        playerCache.put(targetPlayer.getUniqueId(), player);

        sender.sendMessage(ChatColor.GRAY + args[0] + " has been muted");

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 1) {
            return null;
        }

        Collection<? extends Player> onlinePlayers = sender.getServer().getOnlinePlayers();

        return onlinePlayers.stream()
                .filter(player -> player.getName().startsWith(args[0]))
                .map(player -> player.getName())
                .collect(Collectors.toList());
    }
}
