package com.pcb.pcbridge.ban.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.pcb.pcbridge.library.controllers.listeners.AbstractListener;

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{
		_plugin.getLogger().info("player joined");
		
		String username = e.getPlayer().getName();
		String uuid 	= e.getPlayer().getUniqueId().toString();

			
		String message = "Åòc" + "You are currently banned.\n\n" +
			
						 "Åò8" + "Reason: Åòf" + "test" + "\n" +
						 "Åò8" + "Expires: Åòf" + "Never" + "\n\n" + 
								 
						 "Åòb" + "Appeal @ www.projectcitybuild.com";
				
		//message = message.replaceAll("&","Åò");
		//e.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);	
	}
}
