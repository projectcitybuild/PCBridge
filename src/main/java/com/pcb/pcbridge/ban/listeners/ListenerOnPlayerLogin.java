package com.pcb.pcbridge.ban.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.library.controllers.listeners.AbstractListener;
import com.pcb.pcbridge.library.database.AbstractAdapter;

/**
 * Check if a player is banned upon entry to the server
 */

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		String username = e.getPlayer().getName();
		String uuid 	= e.getPlayer().getUniqueId().toString();

		AbstractAdapter adapter = _plugin.GetAdapter();
		List<HashMap<String, Object>> results;
		try 
		{
			results = BanHelper.LookupPlayer(adapter, username, uuid);
		} 
		catch (SQLException err) 
		{
			_plugin.getLogger().severe("Could not look up player's ban record on entry: " + err.getMessage());
			return;
		}
		
		if(results == null || results.size() == 0)
			return;
		
		HashMap<String, Object> ban = results.get(0);
		String message = "Åòc" + "You are currently banned.\n\n" +
			
						 "Åò8" + "Reason: Åòf" + ban.get("reason") + "\n" +
						 "Åò8" + "Expires: Åòf" + "Never" + "\n\n" + 
								 
						 "Åòb" + "Appeal @ www.projectcitybuild.com";
			
		// TODO: display expiry time if temp ban
		
		e.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);	
	}
}
