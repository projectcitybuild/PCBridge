package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.bukkit.ban.BanCache;
import com.pcb.pcbridge.bukkit.ban.BanHelper;
import com.pcb.pcbridge.bukkit.ban.BanQueueItem;
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
	public boolean Execute(CommandArgs e) 
	{
		final String username = e.Args[0];
		
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		// check if specified player is already banned
		BanCache cache = _plugin.GetBanCache();
		List<Ban> entries = cache.Get(username);
		
		Boolean isBanned = false;
		if(entries != null)
		{
			ListIterator<Ban> i = entries.listIterator();		
			while(i.hasNext())
			{
				Ban entry = i.next();
				
				if(entry.IsActive)
				{
					isBanned = true;
					entry.IsActive = false;
				}
			}
		}
				
		if(!isBanned)
		{
			MessageHelper.Send(MessageType.INFO, e.Sender, username + " is not currently banned.");
			return true;
		}		
		
		
		// remove player from ban cache
		final CommandSender sender = e.Sender;
		cache.Set(username, entries, new BanQueueItem() {
			@Override
			public void OnProcess() 
			{
				// queue operation to remove player from ban database
				try 
				{
					AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
					adapter.Execute("UPDATE banlist SET is_active='0' WHERE name=?",
						username
					);
					
					_plugin.getLogger().info(username + " unregistered from the ban database.");
				} 
				catch (SQLException err) 
				{
					MessageHelper.Send(MessageType.FATAL, sender, "Failed to remove ban from database.");
					_plugin.getLogger().severe("Failed to remove ban from database: " + err.getMessage());
					err.printStackTrace();
				}
			}
		});
		
		_plugin.getServer().broadcastMessage(ChatColor.GRAY + username + " has been unbanned by " + sender.getName() + ".");
		_plugin.getLogger().info(username + " removed from ban cache by " + sender.getName());
		
		
		return true;
	}
	
}
