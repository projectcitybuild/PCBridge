package com.pcb.pcbridge.ranks.listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Synchronises player's rank with forums upon login
 */

public final class ListenerOnPlayerLogin extends AbstractListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLogin(PlayerLoginEvent e)
	{		
		Player player = e.getPlayer();
		
		// retrieve player's group ids from forum storage
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> results = null;
		try
		{
			adapter.UseDatabase( _plugin.getConfig().getString("database.forum.database") );
			results = adapter.Query("SELECT id_group, additional_groups FROM smf_members WHERE real_name=?", player.getDisplayName());
			adapter.UseDatabase( _plugin.getConfig().getString("database.remote.database") );
		}
		catch(SQLException err)
		{
			err.printStackTrace();
		}
		
		if(results == null || results.size() == 0)
		{
			System.out.println("No forum account found");
			return;
		}
		
		// split secondary group ids by comma delimiter and then combine primary & secondary
		HashMap<String, Object> row = results.get(0);
		int primaryGroup = (int)row.get("id_group");
		String secondaryGroups = (String)row.get("additional_groups");

		List<String> groups = new ArrayList<String>( Arrays.asList(secondaryGroups.split(",")) );
		groups.add(Integer.toString(primaryGroup));
		
		// remove all groups first
		String[] allGroups = _plugin.GetPermissions().getPlayerGroups(player);
		for(String group : allGroups)
		{
			_plugin.GetPermissions().playerRemoveGroup(player, group);
		}
		
		// hardcoded for now...
		for(String id : groups)
		{
			switch(id)
			{
			case "1":
				_plugin.GetPermissions().playerAddGroup(player, "Admin");
				break;
			case "13":
				_plugin.GetPermissions().playerAddGroup(player, "SOP");
				break;
			case "2":
				_plugin.GetPermissions().playerAddGroup(player, "OP");
				break;
			case "14":
				_plugin.GetPermissions().playerAddGroup(player, "Moderator");
				break;
			case "21":
				_plugin.GetPermissions().playerAddGroup(player, "Trusted");
				break;
			case "11":
				_plugin.GetPermissions().playerAddGroup(player, "Donator");
				break;
			default:
				_plugin.GetPermissions().playerAddGroup(player, "Member");
				break;
			}
		}
		
	}
}
