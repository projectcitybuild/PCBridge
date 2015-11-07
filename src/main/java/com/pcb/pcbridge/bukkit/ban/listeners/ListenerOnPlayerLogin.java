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
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.controllers.AbstractListener;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Check if a player is banned upon entry to the server
 */

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		/*String username = e.getPlayer().getName();
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
		
		// has the ban expired?
		String expiry = "Never";
		Object expiryTS = results.get(0).get("date_expire");
		boolean isTempBan = expiryTS != null;
		if(isTempBan)
		{
			int timestamp = (int)results.get(0).get("date_expire");
			expiry = new Date(timestamp * 1000L).toString();
			
			long now = (long) (new Date().getTime() / 1000L);
			if(now >= timestamp)
			{
				long banId = (long)results.get(0).get("id");
				
				// ban has expired
				try 
				{
					adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE id=?", banId);
					adapter.Execute(
						new QueryBuilder().Insert("pcban_unbans")
							.Field("ban_id", banId)
							.Field("date", timestamp)
							.Build()
					);
				} 
				catch (SQLException err) 
				{
					_plugin.getLogger().severe("Could not remove expired ban on entry (but still let player in): " + err.getMessage());
				}
				
				return;
			}
		}
		
		HashMap<String, Object> ban = results.get(0);
		String message = "Åòc" + "You are currently banned.\n\n" +
			
						 "Åò8" + "Reason: Åòf" + ban.get("reason") + "\n" +
						 "Åò8" + "Expires: Åòf" + expiry + "\n\n" + 
								 
						 "Åòb" + "Appeal @ www.projectcitybuild.com";
		
		e.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);	*/
	}
}
