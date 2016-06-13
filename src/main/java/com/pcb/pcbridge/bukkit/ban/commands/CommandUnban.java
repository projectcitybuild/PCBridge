package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.BanHelper;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.async.IFutureCallback;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Unbans the specified player
 */

public final class CommandUnban extends AbstractCommand 
{	
	public boolean Execute(final CommandArgs e) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		// check if specified player is already banned
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> result;
		try 
		{
			result = adapter.Query("SELECT * FROM banlist WHERE name=? LIMIT 0,1",
				e.Args[0]
			);
					
			if(result == null && result.size() <= 0)
			{
				MessageHelper.Send(MessageType.INFO, e.Sender, e.Args[0] + " is not currently banned.");
				return true;
			}
		} 
		catch (SQLException err) 
		{
			MessageHelper.Send(MessageType.FATAL, e.Sender, "Failed to lookup ban entry.");
			_plugin.getLogger().severe("Failed to lookup ban entry: " + err.getMessage());
			err.printStackTrace();
		}
		
		
		// unban the player
		try 
		{
			adapter.Execute("DELETE FROM banlist WHERE name=?",
				e.Args[0]
			);
			
			_plugin.getServer().broadcastMessage(ChatColor.GRAY + e.Args[0] + " has been unbanned by " + e.Sender.getName() + ".");
			_plugin.getLogger().info(e.Args[0] + " has been unbanned by " + e.Sender.getName());
		} 
		catch (SQLException err) 
		{
			MessageHelper.Send(MessageType.FATAL, e.Sender, "Failed to remove ban from database.");
			_plugin.getLogger().severe("Failed to remove ban from database: " + err.getMessage());
			err.printStackTrace();
		}
		
		
		return true;
	}
	
}
