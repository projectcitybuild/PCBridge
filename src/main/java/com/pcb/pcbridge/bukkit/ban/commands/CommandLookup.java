package com.pcb.pcbridge.bukkit.ban.commands;

import java.util.Date;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.bukkit.ban.BanCache;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;

/**
 * Command: Retrieves data about the specified player (eg. whether currently banned, ban reason, etc)
 * 
 */

public final class CommandLookup extends AbstractCommand 
{	
	public boolean Execute(CommandArgs e) 
	{		
		String username = e.Args[0];
		
		if(e.Args.length < 1)
			return false;
		
		// check the ban cache for an entry under the given username
		BanCache cache = _plugin.GetBanCache();
		Ban ban = cache.Get(username);
		
		if(ban != null)
		{			
			Date banDate = TimestampHelper.GetDateFromTimestamp(ban.BanDate);
				
			String msg = ChatColor.DARK_RED + username + " is currently banned.\n\n" +
					ChatColor.GRAY + "---\n" +
					ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + ban.Reason + "\n" +
					ChatColor.GRAY + "---\n" +
					ChatColor.YELLOW + "Banned by: " + ChatColor.GRAY + ban.StaffName + "\n" +
					ChatColor.YELLOW + "Date: " + ChatColor.GRAY + banDate + "\n" +
					ChatColor.YELLOW + "Expiry Date: " + ChatColor.GRAY + "Never";
					
			MessageHelper.Send(MessageType.INFO, e.Sender, msg);
		}
		else
		{
			MessageHelper.Send(MessageType.INFO, e.Sender, username + " is not banned.");
		}
						
		return true;
	}	
}
