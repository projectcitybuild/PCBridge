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
		
		// first run? generate tables
		if(plugin.getConfig().getBoolean("database.first_run"))
		{
			if(GenerateTables())
			{
				plugin.getConfig().set("database.first_run", false);
				plugin.saveConfig();
			}		
			
			// test connection on boot
			TestConnection();	
		}
		else
		{
			// test connection on boot
			TestConnection();	
		}
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
	
	/**
	 * Generate the database tables
	 * 
	 * @return	True if generation success; False if failed
	 */
	private boolean GenerateTables()
	{
		String sql = "CREATE TABLE IF NOT EXISTS pcban_active_bans ("
				  + "id int(11) unsigned NOT NULL AUTO_INCREMENT,"
				  + "banned_uuid varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,"
				  + "banned_name varchar(?) NOT NULL,"
				  + "date_ban int(8) NOT NULL,"
				  + "date_expire int(8) DEFAULT NULL,"
				  + "staff_uuid varchar(60) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,"
				  + "staff_name varchar(50) NOT NULL,"
				  + "reason text NOT NULL,"
				  + "ip varchar(15) NOT NULL,"
				  + "is_active tinyint(1) NOT NULL DEFAULT '1',"
				  + "PRIMARY KEY (id)"
				  + ") ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20 ;";
		
		try 
		{
			_adapter.Execute(sql, 50);
		} 
		catch (SQLException err) 
		{
			_plugin.getLogger().severe("Could not generate tables: " + err.getMessage());
			return false;
		}
		
		return true;
	}
}
