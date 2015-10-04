package com.pcb.pcbridge.library.database;

import java.sql.SQLException;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.PCBridge;

/**
 * Factory for different Storage implementations
 * (currently only supports MySQL)
 */

public final class ConnectionManager 
{			
	private AbstractAdapter _adapter;
	
	public ConnectionManager(PCBridge plugin, Adapter adapter)
	{
		String address 	= plugin.getConfig().getString("database.address");
		String port 	= plugin.getConfig().getString("database.port");
		String username = plugin.getConfig().getString("database.username");
		String password = plugin.getConfig().getString("database.password");
		
		switch(adapter)
		{
			default:
			case MYSQL:
				_adapter = new AdapterMySQL(address, port, "pcbridge", username, password);
				break;
		}
		
		// test connection on boot
		if(plugin.getConfig().getBoolean("database.boot_test_connection"))
		{
			try
			{
				_adapter.Query("SELECT * FROM pcban_active_bans LIMIT 0,?", 1);
			}
			catch(SQLException err)
			{
				plugin.getLogger().severe("Could not connect to database: " + err.getMessage());
			}
		}
	}

	public AbstractAdapter GetAdapter()
	{
		return _adapter;
	}
}
