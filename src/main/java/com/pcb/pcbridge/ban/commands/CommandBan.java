package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;

public final class CommandBan implements ICommand 
{	
	/**
	 * Ban the specified user (via username) from the server
	 */
	public boolean Execute(CommandPacket e) 
	{
		if(e.Args.length == 0)
			return false;
		
		String playerName = e.Args[0];
		Player player = e.Plugin.getServer().getPlayer(playerName);
		
		String playerUUID = "";
		String playerIP = "";
		OfflinePlayer offlinePlayer;
		if(player == null)
		{
			offlinePlayer 	= e.Plugin.getServer().getOfflinePlayer(playerName);
			playerUUID 		= offlinePlayer.hasPlayedBefore() ? offlinePlayer.getUniqueId().toString() : "";
			
			// TODO: perhaps async lookup the actual UUID via Mojang's server?
		}
		else
		{
			playerUUID 	= player.getUniqueId().toString();
			playerIP 	= player.getAddress().getHostString();
		}
		
		// if given, stitch together the 'ban reason' which spans multiple args
		String banReason = "Griefing";
		if(e.Args.length > 1)
		{
			StringBuilder builder = new StringBuilder();
			for(int x=1; x<e.Args.length; x++)
			{
				if(x > 1)
					builder.append(" ");
				
				builder.append(e.Args[x]);
			}
			banReason = builder.toString();
		}
		
		String staffName = e.Sender.getName();
		String staffUUID = e.IsPlayer ? e.Plugin.getServer().getPlayer(staffName).getUniqueId().toString() : "";
		
		
		AbstractAdapter adapter = e.Plugin.GetAdapter();

		// check if the user is already banned
		boolean isBanned = false;
		try
		{
			isBanned = BanHelper.IsPlayerBanned(adapter, playerName, playerUUID);
		}
		catch(SQLException err)
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not look up player in ban records. Aborting");
			e.Plugin.getLogger().severe("Could not look up player in ban records: " + err.getMessage());
			return true;
		}
		
		if(isBanned)
		{
			e.Sender.sendMessage(ChatColor.GRAY + playerName + " is already banned.");
			return true;
		}
		
		// create the ban in storage
		Date currentDate = new Date();
		long now = currentDate.getTime() / 1000;		
		
		try 
		{
			adapter.Execute("INSERT INTO pcban_active_bans(banned_name, banned_uuid, date_ban, staff_uuid, staff_name, reason, ip) VALUES (?, ?, ?, ?, ?, ?, ?)",
					playerName, 
					playerUUID, 
					now,
					staffUUID,
					staffName,
					banReason, 
					playerIP
			);
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not ban user. Please notify an administrator if this persists");
			e.Plugin.getLogger().severe("Could not add user to ban list: " + err.getMessage());
		}
		
		// TODO: kick player if they're currently online !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		e.Sender.sendMessage(ChatColor.GRAY + playerName + " has been banned.");
		
		return true;
	}
	
}
