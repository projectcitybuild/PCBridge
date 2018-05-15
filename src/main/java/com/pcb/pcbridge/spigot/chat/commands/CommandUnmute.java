package com.pcb.pcbridge.spigot.chat.commands;

import com.google.inject.Inject;
import com.pcb.pcbridge.features.players.PlayerCache;
import com.pcb.pcbridge.features.players.models.PlayerModel;
import com.pcb.pcbridge.framework.commands.AbstractCommand;
import com.pcb.pcbridge.framework.helpers.StringHelpers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandUnmute extends AbstractCommand {

    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public String getPermissionNode() {
        return "pcbridge.chat.mute";
    }


    private PlayerCache playerCache;

    @Inject
    public CommandUnmute(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    @Override
    public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 1) {
            return false;
        }

        // check that the given player name is online
        final Optional<Player> potentialPlayer = sender.getServer().getOnlinePlayers().stream()
                .map(player -> (Player)player)
                .filter(player -> player.getName().equalsIgnoreCase(args[0]))
                .findFirst();

        if(!potentialPlayer.isPresent()) {
            sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found");
            return true;
        }
        final Player targetPlayer = potentialPlayer.get();

        PlayerModel player = playerCache.get(targetPlayer.getUniqueId(), () -> {
            PlayerModel newPlayer = new PlayerModel(targetPlayer.getUniqueId());
            newPlayer.load();

            return newPlayer;
        });

        if(!player.getIsMuted()) {
            sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is not currently muted");
            return true;
        }

        try {
            player.setIsMuted(false);
            player.save();
        } catch(IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to write to file. Please contact an admin");
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + args[0] + " has been unmuted");

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
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
