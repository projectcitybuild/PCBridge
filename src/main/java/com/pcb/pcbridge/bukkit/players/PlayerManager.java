package com.pcb.pcbridge.bukkit.players;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * A registry of users currently online and their PCBridge settings
 */

public final class PlayerManager 
{
	private HashMap<UUID, PlayerData> _players = new HashMap<UUID, PlayerData>();
	private PCBridge _plugin;
	
	public PlayerManager(PCBridge plugin)
	{
		this._plugin = plugin;
	}
	
	/**
	 * Gets all players currently online and retrieves their individual settings from storage
	 */
	public void BuildPlayerList()
	{		
		_players.clear();
		
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			BuildPlayer(player);
		}
	}
	
	/**
	 * Retrieves settings from storage for the specified player
	 * 
	 * @param uuid
	 * @return PlayerData
	 */
	public PlayerData BuildPlayer(Player player)
	{
		UUID uuid = player.getUniqueId();
		
		// retrieve settings from storage
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.LOCAL);
		List<HashMap<String, Object>> results = null;
		try
		{
			results = adapter.Query(
					"SELECT pcbridge_users.id, swear_filter_on FROM pcbridge_users "
					+ "INNER JOIN pcbridge_swear_filter ON user_id = pcbridge_users.id WHERE uuid=? "
					, uuid.toString());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
					
		// create new entry in Users table if they aren't there
		PlayerData playerData;
		if(results == null || results.size() == 0)
		{
			try 
			{
				int id = adapter.Execute("INSERT INTO pcbridge_users (uuid, last_known_name) VALUES (?, ?)", uuid.toString(), player.getDisplayName());
				adapter.Execute("INSERT INTO pcbridge_swear_filter (user_id, swear_filter_on) VALUES (?, ?)", id, false);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
						
			playerData = new PlayerData();
		}
		else
		{
			HashMap<String, Object> row = results.get(0);
			long userId 		= (long)row.get("id");
			boolean swearFilter = (boolean)row.get("swear_filter_on");
			
			playerData = new PlayerData(userId, swearFilter);
		}

		_players.put(uuid, playerData);
		return playerData;
	}
	
	/**
	 * Remove the specified uuid from the 'users online' registry
	 * 
	 * @param uuid
	 */
	public void RemovePlayer(UUID uuid)
	{
		_players.remove(uuid);
	}
	
	/**
	 * Gets the settings for the specified player's uuid
	 * 
	 * @param uuid
	 * @return
	 */
	public PlayerData GetSettings(UUID uuid)
	{
		PlayerData settings = _players.get(uuid);
		
		if(settings != null)
			return settings;
		
		return BuildPlayer( Bukkit.getServer().getPlayer(uuid) );
	}
	
	/**
	 * Saves the settings as they are of the specified player back to storage
	 * 
	 * @param uuid
	 */
	public void SaveSettings(UUID uuid)
	{
		PlayerData settings = _players.get(uuid);
		
		if(settings == null)
		{
			_plugin.getLogger().warning("Attempted to save PlayerData of non existant registry player");
			return;
		}
			
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.LOCAL);
		try
		{
			adapter.Execute("UPDATE pcbridge_swear_filter SET swear_filter_on=? WHERE user_id=?", settings.SwearFilter, settings.UserId);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
