package com.pcb.pcbridge.library.database;

import java.sql.SQLException;
import java.util.HashMap;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;
import com.pcb.pcbridge.library.database.adapters.Adapter;
import com.pcb.pcbridge.library.database.adapters.AdapterMySQL;

/**
 * Factory for different Storage implementations.
 * Also acts as an access point to separate database connections
 */

public final class ConnectionManager 
{			
	private HashMap<DbConn, AbstractAdapter> _adapters = new HashMap<DbConn, AbstractAdapter>();
	private final PCBridge _plugin;
	
	public ConnectionManager(PCBridge plugin)
	{
		this._plugin = plugin;
	}

	/**
	 * Returns an adapter for use by config name
	 * 
	 * @param name
	 * @return
	 */
	public AbstractAdapter GetAdapter(DbConn name)
	{
		AbstractAdapter adapter = _adapters.get(name);
		assert(adapter != null);
		
		return adapter;
	}
	
	/**
	 * Adds an adapter to the list of available connections
	 * 
	 * @param name
	 * @param adapter
	 */
	public ConnectionManager AddAdapter(DbConn name, Adapter adapterType)
	{	
		String conn = name.toString().toLowerCase();
		
		String address 	= _plugin.getConfig().getString("database." + conn + ".address");
		String port 	= _plugin.getConfig().getString("database." + conn + ".port");
		String username = _plugin.getConfig().getString("database." + conn + ".username");
		String password = _plugin.getConfig().getString("database." + conn + ".password");
		String database = _plugin.getConfig().getString("database." + conn + ".database");
		
		AbstractAdapter adapter;
		
		switch(adapterType)
		{
			default:
			case MYSQL:
				adapter = new AdapterMySQL(address, port, database, username, password);
				break;
		}
		
		_adapters.put(name, adapter);
		
		// first run? generate tables
		if(_plugin.getConfig().getBoolean("database.first_run"))
		{
			if(GenerateTables(adapter))
			{
				_plugin.getConfig().set("database.first_run", false);
				_plugin.saveConfig();
			}		
		}
		
		// test connection on boot
		TestConnection(adapter, name);	
		
		// TODO: revert to file adapter storage if db connection failed
		
		return this;
	}
		
	/**
	 * Attempt a basic (synchronous) query to test the database connection.
	 * Recommended ON if using a remote connection as the connection gets pooled during boot.
	 */
	private void TestConnection(AbstractAdapter adapter, DbConn name)
	{
		/*if(_plugin.getConfig().getBoolean("database.boot_test_connection"))
		{
			try
			{
				// TODO: replace these hardcoded tests
				if(name == DbConn.REMOTE)
				{
					adapter.Query("SELECT * FROM pcbridge_active_bans LIMIT 0,?", 1);
				}
				else
				{
					adapter.Query("SELECT * FROM pcbridge_users LIMIT 0,?", 1);
				}
				
				_plugin.getLogger().info("DB connection test succeeded");
			}
			catch(SQLException err)
			{
				_plugin.getLogger().severe("Could not connect to database: " + err.getMessage());
			}
		}*/
	}
	
	/**
	 * Generate the database tables
	 * 
	 * @return	True if generation success; False if failed
	 */
	private boolean GenerateTables(AbstractAdapter adapter)
	{
		String sql = "CREATE TABLE IF NOT EXISTS banlist ("
				  + "name varchar(32) NOT NULL,"
				  + "reason text NOT NULL,"
				  + "admin varchar(32) NOT NULL,"
				  + "time bigint(20) NOT NULL,"
				  + "temptime bigint(20) NOT NULL DEFAULT '0',"
				  + "type int(11) NOT NULL DEFAULT '0',"
				  + "id int(11) unsigned NOT NULL AUTO_INCREMENT,"
				  + "ip varchar(16) DEFAULT NULL,"
				  + "is_active tinyint(1) NOT NULL DEFAULT '1',"
				  + "PRIMARY KEY (id)"
				  + ") ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=0 ;";
		
		try 
		{
			adapter.Execute(sql, 50);
		} 
		catch (SQLException err) 
		{
			_plugin.getLogger().severe("Could not generate tables: " + err.getMessage());
			return false;
		}
		
		return true;
	}
}
