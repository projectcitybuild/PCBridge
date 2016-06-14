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

import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.bukkit.ban.BanCache;
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

		BanCache cache = _plugin.GetBanCache();
		Ban entry = cache.Get(username);
		
		if(entry != null)
		{
			String message = "Åòc" + "You are currently banned.\n\n" +
					
							 "Åò8" + "Reason: Åòf" + entry.Reason + "\n" +
							 "Åò8" + "Expires: Åòf" + "Never" + "\n\n" + 
									 
							 "Åòb" + "Appeal @ www.projectcitybuild.com";
			
			e.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
		}
		
	}
}
