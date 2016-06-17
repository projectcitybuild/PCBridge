package com.pcb.pcbridge.bukkit.utility.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import com.pcb.pcbridge.library.controllers.AbstractListener;


public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	/**
	 * Check if the server is in maintenance mode.
	 * Only allow players with the permission node in
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		Player player = e.getPlayer();

		Boolean isMaintenanceMode = _plugin.getConfig().getBoolean("settings.maintenance_mode");
		if(isMaintenanceMode)
		{
			if(!player.hasPermission("pcbridge.maintenance.access"))
			{
				String message = "Server maintenance in progress.\n\nVisit www.projectcitybuild.com for details.";
				e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, message);
			}
		}
		
	}
}
