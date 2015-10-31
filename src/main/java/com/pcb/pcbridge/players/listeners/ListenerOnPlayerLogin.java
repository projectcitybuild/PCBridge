package com.pcb.pcbridge.players.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.pcb.pcbridge.library.controllers.listeners.AbstractListener;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Ensures a player has a row in the Users table upon entry
 */

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		Player player = e.getPlayer();
		
		_plugin.GetPlayerManager().BuildPlayer(player);
	}
}
