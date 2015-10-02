package com.pcb.pcbridge.library.database;

import com.pcb.pcbridge.PCBridge;

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
		
	}

	public AbstractAdapter GetAdapter()
	{
		return _adapter;
	}
}
