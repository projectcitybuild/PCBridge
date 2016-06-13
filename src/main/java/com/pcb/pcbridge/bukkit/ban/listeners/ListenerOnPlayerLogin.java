package com.pcb.pcbridge.bukkit.ban.listeners;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.pcb.pcbridge.bukkit.ban.BanHelper;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.controllers.AbstractListener;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Check if a player is banned upon entry to the server
 */

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		String username = e.getPlayer().getName();

		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> result;
		try 
		{
			result = adapter.Query("SELECT * FROM banlist WHERE name=? LIMIT 0,1",
				username
			);
			
			if(result.size() <= 0 || result == null)
				return;
			
			HashMap<String, Object> ban = result.get(0);
			String message = "Åòc" + "You are currently banned.\n\n" +
				
							 "Åò8" + "Reason: Åòf" + ban.get("reason") + "\n" +
							 "Åò8" + "Expires: Åòf" + "Never" + "\n\n" + 
									 
							 "Åòb" + "Appeal @ www.projectcitybuild.com";
			
			e.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
		} 
		catch (SQLException err) 
		{
			_plugin.getLogger().severe("Failed to lookup ban entry on server join: " + err.getMessage());
			err.printStackTrace();
		}
	}
}
