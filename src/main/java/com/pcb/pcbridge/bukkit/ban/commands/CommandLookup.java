package com.pcb.pcbridge.bukkit.ban.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

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
		List<Ban> entries = cache.Get(username);
		
		Ban ban = null;
		List<Ban> historyEntries = new ArrayList<Ban>();
		if(entries != null)
		{
			ListIterator<Ban> i = entries.listIterator();		
			while(i.hasNext())
			{
				Ban entry = i.next();
				
				if(entry.IsActive)
				{
					ban = entry;
				}
				else
				{
					historyEntries.add(entry);
				}
			}
		}
		
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
			MessageHelper.Send(MessageType.INFO, e.Sender, ChatColor.GREEN + username + " is not banned.");
		}
		
		
		// also display any previous bans if they exist
		if(historyEntries.size() > 0)
		{
			MessageHelper.Send(MessageType.INFO, e.Sender, ChatColor.GRAY + "---\n");
			MessageHelper.Send(MessageType.INFO, e.Sender, ChatColor.GRAY + "Found " + historyEntries.size() + 
					" previous " + MessageHelper.Plural("ban", historyEntries.size()) + "\n");
			
			ListIterator<Ban> i = historyEntries.listIterator();		
			while(i.hasNext())
			{
				Ban entry = i.next();
				
				Date banDate = TimestampHelper.GetDateFromTimestamp(entry.BanDate);
				
				String msg = ChatColor.GRAY + "---\n" +
						ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + entry.Reason + "\n" +
						ChatColor.YELLOW + "Banned by: " + ChatColor.GRAY + entry.StaffName + ChatColor.GRAY + " | " + 
						ChatColor.YELLOW + "Unbanned by: " + ChatColor.GRAY + entry.UnbannedBy + "\n" +
						ChatColor.YELLOW + "Date: " + ChatColor.GRAY + banDate + "\n";
						
				MessageHelper.Send(MessageType.INFO, e.Sender, msg);
			}
		}
		
						
		return true;
	}	
}
