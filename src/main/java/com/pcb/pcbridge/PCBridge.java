package com.pcb.pcbridge;

import org.bukkit.plugin.java.JavaPlugin;

import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.ControllerManager;
import com.pcb.pcbridge.library.database.ConnectionManager;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;
import com.pcb.pcbridge.library.database.adapters.Adapter;

/**
 * 
 * A plugin to bridge www.projectcitybuild.com and its servers
 * 
 * @author		Andy Saw <andy-saw@hotmail.com>
 * @created		19th September, 2015
 * 
 */

public final class PCBridge extends JavaPlugin 
{
	private ControllerManager _controllerManager;
	private ConnectionManager _connectionManager;
	private UUIDLookup _uuidLookup;
	
	public AbstractAdapter GetAdapter()
	{
		return _connectionManager.GetAdapter();
	}
	
	public ControllerManager GetControllerManager()
	{
		return _controllerManager;
	}
	
	public UUIDLookup GetUUIDLookup()
	{
		return _uuidLookup;
	}
	
	@Override
	public void onEnable()
	{
		LoadConfig();
		
		_connectionManager = new ConnectionManager(this, Adapter.MYSQL);
		_controllerManager = new ControllerManager(this);
		_uuidLookup = new UUIDLookup();
	}
	
	@Override
	public void onDisable()
	{
	}
	
	/**
	 * Adds any missing config entries to the config
	 * (this should probably be moved into its own class if it gets any more complex)
	 */
	private void LoadConfig()
	{
		getConfig().addDefault("database.address", "localhost");
		getConfig().addDefault("database.port", 3306);
		getConfig().addDefault("database.username", "root");
		getConfig().addDefault("database.password", "");
		getConfig().addDefault("database.boot_test_connection", true);
		getConfig().addDefault("database.first_run", true);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
}
