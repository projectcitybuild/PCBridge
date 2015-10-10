package com.pcb.pcbridge.library.database;

import java.sql.SQLException;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;
import com.pcb.pcbridge.library.database.adapters.Adapter;
import com.pcb.pcbridge.library.database.adapters.AdapterMySQL;

/**
 * Factory for different Storage implementations
 * (currently only supports MySQL)
 */

public final class ConnectionManager 
{			
	private final AbstractAdapter _adapter;
	private final PCBridge _plugin;
	
	public ConnectionManager(PCBridge plugin, Adapter adapter)
	{
		this._plugin = plugin;
		
		String address 	= plugin.getConfig().getString("database.address");
		String port 	= plugin.getConfig().getString("database.port");
		String username = plugin.getConfig().getString("database.username");
		String password = plugin.getConfig().getString("database.password");
		
		switch(adapter)
		{
			default:
			case MYSQL:
				_adapter = new AdapterMySQL(plugin, address, port, "pcbridge", username, password);
				break;
		}
		
		// test connection on boot
		TestConnection();		
	}

	public AbstractAdapter GetAdapter()
	{
		return _adapter;
	}
	
	/**
	 * Attempt a basic (synchronous) query to test the database connection.
	 * Recommended ON if using a remote connection as the connection gets pooled during boot.
	 */
	private void TestConnection()
	{
		if(_plugin.getConfig().getBoolean("database.boot_test_connection"))
		{
			try
			{
				_adapter.Query("SELECT * FROM pcban_active_bans LIMIT 0,?", 1);
				_plugin.getLogger().info("DB connection test succeeded");
			}
			catch(SQLException err)
			{
				_plugin.getLogger().severe("Could not connect to database: " + err.getMessage());
			}
		}
	}
}
