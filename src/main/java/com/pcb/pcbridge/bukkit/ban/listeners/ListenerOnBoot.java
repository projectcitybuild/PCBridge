package com.pcb.pcbridge.bukkit.ban.listeners;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.bukkit.ban.Ban;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * A special listener that invokes when PCBridge is enabled via onEnable()
 */

public class ListenerOnBoot 
{
	private PCBridge _plugin;
	
	public ListenerOnBoot(PCBridge plugin)
	{
		this._plugin = plugin;
	}
	
	/**
	 * Retrieves all ban records and stores it in cache
	 * 
	 * @param plugin
	 */
	public void Execute()
	{
		_plugin.getLogger().info("Building ban list cache...");
		
		AbstractAdapter adapter = _plugin.GetAdapter(DbConn.REMOTE);
		List<HashMap<String, Object>> result;
		try 
		{
			result = adapter.Query("SELECT * FROM banlist WHERE id>?",
				-1
			);	
		} 
		catch (SQLException err) 
		{
			_plugin.getLogger().severe("Failed to retrieve ban records for caching: " + err.getMessage());
			err.printStackTrace();
			return;
		}		
		
		int x = 0;
		ListIterator<HashMap<String, Object>> i = result.listIterator();		
		while(i.hasNext())
		{
			HashMap<String, Object> row = i.next();
			Ban ban = new Ban(row);
			_plugin.GetBanCache().Remember(ban.Name, ban, null);
			x++;
		}
		
		_plugin.getLogger().info("Build complete - " + x + " entries in memory");
	}	
	
}
