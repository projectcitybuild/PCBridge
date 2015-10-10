package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.controllers.commands.CommandArgs;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Unbans the specified player
 */

public final class CommandUnban implements ICommand 
{	
	public boolean Execute(CommandArgs e, Object... args) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		String username = e.Args[0];
		PlayerUUID player = BanHelper.GetUUID(e.Plugin, username);
		
		// retrieve ban from storage
		AbstractAdapter adapter = e.Plugin.GetAdapter();
		boolean isBanned;
		try 
		{
			isBanned = BanHelper.IsPlayerBanned(adapter, username, player.GetUUID());
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not lookup player in ban records.");
			e.Plugin.getLogger().severe("Could not lookup player in ban records: " + err.getMessage());
			return true;
		}
				
		if(!isBanned)
		{
			e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " is not currently banned.");
			return true;
		}
		
		try 
		{
			adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE banned_name=?",
					e.Args[0]
			);
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not unban player.");
			e.Plugin.getLogger().severe("Could not unban player: " + err.getMessage());
			return true;
		}
		
		e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " has been unbanned.");
		return true;
	}	
}
