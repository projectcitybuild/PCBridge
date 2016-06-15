package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.bukkit.ban.BanCache;
import com.pcb.pcbridge.bukkit.ban.BanQueueItem;
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
		String username = e.Args[0];
		
		// check if specified player is already banned
		BanCache cache = _plugin.GetBanCache();
		List<Ban> entries = cache.Get(username);
		
		if(entries != null)
		{
			Boolean isBanned;
			ListIterator<Ban> i = entries.listIterator();		
			while(i.hasNext())
			{
				Ban entry = i.next();
				
				if(entry.IsActive)
				{
					MessageHelper.Send(MessageType.INFO, e.Sender, username + " is already banned.");
					return true;
				}
			}
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
		Player player = _plugin.getServer().getPlayer(username);
		String ip = player != null ? player.getAddress().getAddress().getHostAddress() : null;
		
		// add ban to cache
		final Ban ban = new Ban(username, TimestampHelper.GetNowTimestamp(), (long) 0, 0, staffName, banReason, ip, true);
		final CommandSender sender = e.Sender;
		cache.Remember(username, ban, new BanQueueItem() {
			@Override
			public void OnProcess() 
			{
				// queue operation to add ban to storage
				try 
				{
					AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
					adapter.Execute("INSERT INTO banlist(name, reason, admin, time, temptime, type, ip, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
							ban.Name, 
							ban.Reason, 
							ban.StaffName,
							ban.BanDate,
							0,
							0,
							ban.IP,
							true
						);
										
					_plugin.getLogger().info(ban.Name + " registered in ban database");
				} 
				catch (SQLException err) 
				{
					MessageHelper.Send(MessageType.FATAL, sender, "Failed to insert new ban record into the database. Player may be able to connect to the server after reboot!");
					_plugin.getLogger().severe("Failed to insert new ban record into the database: " + err.getMessage());
					err.printStackTrace();
				}
			}
		});
		
		_plugin.getServer().broadcastMessage("" + ChatColor.DARK_GRAY + ChatColor.BOLD + ban.Name + ChatColor.RESET + ChatColor.GRAY + " has been banned by " + ban.StaffName + ": " + ban.Reason);
		_plugin.getLogger().info(ban.Name + " added to ban cache by " + ban.StaffName + ": " + ban.Reason);
		
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
