package com.pcb.pcbridge.players.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.pcb.pcbridge.library.controllers.listeners.AbstractListener;

/**
 * Remove a player from the 'users online' registry upon leaving
 */

public final class ListenerOnPlayerLogout extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogout(PlayerQuitEvent e)
	{		
		_plugin.GetPlayerManager().RemovePlayer( e.getPlayer().getUniqueId() );
	}
}
