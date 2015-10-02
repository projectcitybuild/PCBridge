package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;

public final class CommandUnban implements ICommand 
{	
	public boolean Execute(CommandPacket e) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		// grab UUID if player has played before/is online
		
		
		// retrieve ban from storage
		AbstractAdapter adapter = e.Plugin.GetAdapter();
		List<HashMap<String, Object>> results;
		try 
		{
			results = adapter.Query("SELECT * FROM pcban_active_bans WHERE is_active=1 and banned_name=?",
					e.Args[0]
			);
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not retrieve ban data.");
			e.Plugin.getLogger().severe("Could not retrieve ban data: " + err.getMessage());
			return true;
		}
				
		if(results == null || results.size() == 0)
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
			err.printStackTrace();
		}
		
		return false;
	}	
}
