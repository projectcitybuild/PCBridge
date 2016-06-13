package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.async.IFutureCallback;
import com.pcb.pcbridge.library.async.NonBlockingExecutor;
import com.pcb.pcbridge.library.async.NonBlockingFuture;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Ban the specified user (via username) from the server
 * 
 * - Checks if the user is already banned
 * - Kicks the player
 * - Adds their username and UUID to the ban list in storage
 */

public final class CommandBan extends AbstractCommand 
{		
	public boolean Execute(CommandArgs e) 
	{
		if(e.Args.length == 0)
			return false;

		// if invoked via code, it's a temporary ban
		return BanPlayer(e);
	}
	
	/**
	 * Bans the player
	 * 
	 * @param e
	 * @param isTempBan
	 */
	private boolean BanPlayer(CommandArgs e)
	{
		// check if specified player is already banned
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> result;
		try 
		{
			result = adapter.Query("SELECT * FROM banlist WHERE name=? LIMIT 0,1",
				e.Args[0]
			);
			
			if(result != null && result.size() > 0)
			{
				MessageHelper.Send(MessageType.INFO, e.Sender, e.Args[0] + " is already banned.");
				return true;
			}
		} 
		catch (SQLException err) 
		{
			MessageHelper.Send(MessageType.FATAL, e.Sender, "Failed to lookup ban entry.");
			_plugin.getLogger().severe("Failed to lookup ban entry: " + err.getMessage());
			err.printStackTrace();
		}	
		
		
		// if given, stitch together the 'ban reason' which spans multiple args
		String banReason = "Griefing";
		int startIndex = 1;
		if(e.Args.length > startIndex)
		{
			StringBuilder builder = new StringBuilder();
			for(int x = startIndex; x < e.Args.length; x++)
			{
				if(x > startIndex)
					builder.append(" ");
								
				builder.append(e.Args[x]);
			}
			banReason = builder.toString();
		}
		
		String staffName = e.Sender.getName();
		Player player = _plugin.getServer().getPlayer(e.Args[0]);
		String ip = player.getAddress().getAddress().getHostAddress();
		
		// add ban to storage
		try 
		{
			adapter.Execute("INSERT INTO banlist(name, reason, admin, time, temptime, type, ip) VALUES (?, ?, ?, ?, ?, ?, ?)",
					e.Args[0], 
					banReason, 
					staffName,
					TimestampHelper.GetNowTimestamp(),
					0,
					0,
					ip
				);
			
			_plugin.getServer().broadcastMessage("" + ChatColor.DARK_GRAY + ChatColor.BOLD + e.Args[0] + ChatColor.RESET + ChatColor.GRAY + " has been banned by " + staffName + ": " + banReason);
			_plugin.getLogger().info(e.Args[0] + " has been banned by " + staffName + ": " + banReason);
		} 
		catch (SQLException err) 
		{
			MessageHelper.Send(MessageType.FATAL, e.Sender, "Failed to insert new ban record into the database.");
			_plugin.getLogger().severe("Failed to insert new ban record into the database: " + err.getMessage());
			err.printStackTrace();
		}
		
		
		// kick the player if they're online
		if(player != null && player.isOnline())
		{
			String expiry = "Never";
			String message = "Åòc" + "You have been banned.\n\n" +
							
						 	 "Åò8" + "Reason: Åòf" + banReason + "\n" +
							 "Åò8" + "Expires: Åòf" + expiry + "\n\n" + 
										 
							 "Åòb" + "Appeal @ www.projectcitybuild.com";
					
			player.kickPlayer(message);
		}
		
		return true;
	}	
		
}
