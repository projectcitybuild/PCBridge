package com.pcb.pcbridge;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.pcb.pcbridge.bukkit.ban.BanController;
import com.pcb.pcbridge.bukkit.ban.cache.BanCache;
import com.pcb.pcbridge.bukkit.utility.UtilityController;
import com.pcb.pcbridge.bukkit.economy.EconomyController;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.ControllerManager;
import com.pcb.pcbridge.library.database.ConnectionManager;
import com.pcb.pcbridge.library.database.DbConn;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;
import com.pcb.pcbridge.library.database.adapters.Adapter;

/**
 * 
 * A plugin to bridge www.projectcitybuild.com and its servers
 * 
 * @author		Andy Saw <andy-saw@hotmail.com>
 * @created		19th September, 2015
 * @updated		12th June, 2016
 * 
 */

public final class PCBridge extends JavaPlugin 
{
	private ControllerManager _controllerManager;
	private ConnectionManager _connectionManager;
	private UUIDLookup _uuidFetcher;
	private BanCache _banCache;
	
	private Economy _economy = null;
	
	
	public AbstractAdapter GetAdapter(DbConn name)
	{
		return _connectionManager.GetAdapter(name);
	}
	
	public ControllerManager GetControllerManager()
	{
		return _controllerManager;
	}
	
	public UUIDLookup GetUUIDFetcher()
	{
		return _uuidFetcher;
	}
	
	public BanCache GetBanCache()
	{
		return _banCache;
	}
	
	public Economy GetEconomyHook()
	{
		return _economy;
	}
	
	
	@Override
	public void onEnable()
	{
		LoadConfig();
		
		_uuidFetcher = new UUIDLookup();
		
		_connectionManager = new ConnectionManager(this);
		_connectionManager
		  //.AddAdapter(DbConn.LOCAL, Adapter.MYSQL)
			.AddAdapter(DbConn.REMOTE, Adapter.MYSQL);

		_banCache = new BanCache(this);
		
		_controllerManager = new ControllerManager(this);
		_controllerManager.CreateControllers(new AbstractController[] {
			new BanController(),
			new EconomyController(),
			new UtilityController()
		});
		
		if (!HookEconomy()) 
		{
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	}
	
	@Override
	public void onDisable()	
	{
		_banCache = null;
	}
	
	/**
	 * Hooks into any existing economy plugin via the Vault API
	 * 
	 * @return
	 */
	private boolean HookEconomy() 
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null) 
			return false;
		
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) 
		{
			return false;
		}
		_economy = rsp.getProvider();
		return _economy != null;
	}
	
	
	/**
	 * Adds any missing config entries to the config
	 * (this should probably be moved into its own class if it gets any more complex)
	 */
	private void LoadConfig()
	{
		getConfig().addDefault("database.remote.address", "192.184.93.126");
		getConfig().addDefault("database.remote.port", 3306);
		getConfig().addDefault("database.remote.username", "root");
		getConfig().addDefault("database.remote.password", "");
		getConfig().addDefault("database.remote.database", "banlist");
		
		/*getConfig().addDefault("database.local.address", "localhost");
		getConfig().addDefault("database.local.port", 3306);
		getConfig().addDefault("database.local.username", "root");
		getConfig().addDefault("database.local.password", "");
		getConfig().addDefault("database.local.database", "pcbridge_local");*/
		
		getConfig().addDefault("database.boot_test_connection", true);
		getConfig().addDefault("database.first_run", true);
		
		getConfig().addDefault("settings.maintenance_mode", false);
		
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
}
