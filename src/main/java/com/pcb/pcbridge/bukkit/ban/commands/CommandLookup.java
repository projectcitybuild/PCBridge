package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Retrieves data about the specified player (eg. whether currently banned, ban reason, etc)
 * 
 */

public final class CommandLookup extends AbstractCommand 
{	
	public boolean Execute(CommandArgs e) 
	{		
		if(e.Args.length < 1)
			return false;
		
		// look up for a ban entry
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> result;
		try 
		{
			result = adapter.Query("SELECT * FROM banlist WHERE name=? LIMIT 0,1",
				e.Args[0]
			);
					
			if(result == null || result.size() <= 0)
			{
				MessageHelper.Send(MessageType.INFO, e.Sender, e.Args[0] + " is not currently banned.");
				return true;
			}
			
			// player is banned; compile their ban record into a nice message
			HashMap<String, Object> ban = result.get(0);		
			
			long banDateTS 		= (long) ban.get("time");
			String banStaff		= (String) ban.get("admin");
			String banReason 	= (String) ban.get("reason");
			Date banDate 		= TimestampHelper.GetDateFromTimestamp((long)banDateTS);
				
			String msg = ChatColor.DARK_RED + e.Args[0] + " is currently banned.\n\n" +
					ChatColor.GRAY + "---\n" +
					ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + banReason + "\n" +
					ChatColor.GRAY + "---\n" +
					ChatColor.YELLOW + "Banned by: " + ChatColor.GRAY + banStaff + "\n" +
					ChatColor.YELLOW + "Date: " + ChatColor.GRAY + banDate + "\n" +
					ChatColor.YELLOW + "Expiry Date: " + ChatColor.GRAY + "Never";
					
			MessageHelper.Send(MessageType.INFO, e.Sender, msg);
			
		} 
		catch (SQLException err) 
		{
			MessageHelper.Send(MessageType.FATAL, e.Sender, "Failed to lookup ban entry.");
			_plugin.getLogger().severe("Failed to lookup ban entry: " + err.getMessage());
			err.printStackTrace();
		}
						
		return true;
	}	
}
