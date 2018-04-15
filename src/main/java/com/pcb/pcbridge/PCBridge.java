package com.pcb.pcbridge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;

import com.pcb.pcbridge.pcbridge.commands.*;
import com.pcb.pcbridge.pcbridge.listeners.*;
import com.pcb.pcbridge.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.pcbridge.schema.*;
import com.pcb.pcbridge.utils.cache.StaticCache;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandManager;
import com.pcb.pcbridge.utils.database.ConnectionPool;
import com.pcb.pcbridge.utils.database.ConnectionSource;
import com.pcb.pcbridge.utils.database.MigrationHandler;
import com.pcb.pcbridge.utils.database.migrations.CreateBanListMigration;
import com.pcb.pcbridge.utils.database.migrations.CreatePlayersMigration;
import com.pcb.pcbridge.utils.database.migrations.CreateWarningsMigration;
import com.pcb.pcbridge.utils.listeners.AbstractListener;
import com.pcb.pcbridge.utils.listeners.ListenerManager;
import com.pcb.pcbridge.utils.listeners.events.PluginEnabledEvent;

public final class PCBridge extends JavaPlugin {
	
	private CommandManager _commandManager;
	private ListenerManager _listenerManager;
	
	private StaticCache<UUID, PlayerConfig> _playerCache;
	public StaticCache<UUID, PlayerConfig> GetPlayerCache()
	{
		return _playerCache;
	}
	
	private static TaskChainFactory _taskChainFactory;
    public static <T> TaskChain<T> NewChain() 
    {
        return _taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> NewSharedChain(String name) 
    {
        return _taskChainFactory.newSharedChain(name);
    }
    
    private static ConnectionPool _connectionPool;
    public static ConnectionPool GetConnectionPool() 
    {
    	return _connectionPool;
    }
    
    private static VaultHook _vaultHook;
    public static VaultHook GetVaultHook()
    {
    	return _vaultHook;
    }
	
	@Override
    public void onEnable() 
	{
		LoadConfig();
		
		_taskChainFactory = BukkitTaskChainFactory.create(this);
		
		_connectionPool = new ConnectionPool( getLogger() )
			.AddSource( GetConnectionSource("banlist") );
		
		if(!_connectionPool.TestConnections())
		{
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// setup database tables
		PrepareDatabase();
		
		// hook into Vault
		_vaultHook = new VaultHook(this);
		
		Environment env = new Environment(this);
		
		// register commands
		_commandManager = new CommandManager(env, _vaultHook.GetPermission(), new AbstractCommand[] {
				new CommandBan(),
				new CommandUnban(),
				new CommandTempBan(),
				new CommandCheckBan(),
				new CommandShowBans(),
				new CommandUuid(),
				new CommandMute(),
				new CommandUnmute(),
				new CommandWarn(),
				new CommandSwearblock(),
				new CommandLogin(),
				new CommandTpLastPos(),
				new CommandPrefix(),
				new CommandSuffix(),
				new CommandRescue(),
				new CommandPCBridge()
        });
		env.SetPromptManager( _commandManager.GetPromptManager() );
		
		// register listeners
		_listenerManager = new ListenerManager(env, new AbstractListener[] {
				new OnPlayerPreLoginEvent(),
				new OnPlayerLoginEvent(),
				new OnPlayerJoinEvent(),
				new OnPluginEnabledEvent(),
				new OnPlayerQuitEvent(),
				new OnPlayerAsyncChatEvent(),
				new OnPlayerNameChangedEvent(),
		});
		
		// create caches
		_playerCache = new StaticCache<>(new ConcurrentHashMap<>());
		
		// call the PluginEnabled event
		getServer().getPluginManager().callEvent( new PluginEnabledEvent() );
    }
    
    @Override
    public void onDisable() 
    {    	
    	_listenerManager.UnregisterAll();
    	_commandManager.UnregisterAll();
    	
    	_taskChainFactory.shutdown(10, TimeUnit.SECONDS);
    	_taskChainFactory = null;
    	
    	_commandManager = null;
    	_listenerManager = null;
    	_playerCache = null;
    	_vaultHook = null;
    	
    	getServer().getScheduler().cancelAllTasks();

    	_connectionPool.Destroy();
    	_connectionPool = null;
    }
    
    /**
     * Ensures all the required db tables exist and
     * runs the appropriate migrations if they don't
     */
    private void PrepareDatabase()
    {
    	try(Connection conn = GetConnectionPool().GetConnection(BanListContract.DATABASE))
    	{		
			MigrationHandler handler = new MigrationHandler(getLogger());	

			List<String> tables = new ArrayList<>();
			try(ResultSet result = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" }) )
			{
				while(result.next())
				{
					tables.add( result.getString("TABLE_NAME") );
				}
			}
			
			// create player list
			if( !tables.contains(PlayerContract.TablePlayers.TABLE_NAME) )
			{
		    	handler.Migrate(conn, CreatePlayersMigration.class);
			}
			
			// create ban list
			if(!tables.contains(BanListContract.TableBans.TABLE_NAME)
					|| !tables.contains(PlayerContract.TablePlayers.TABLE_NAME)
					|| !tables.contains(BanListContract.TableUnbans.TABLE_NAME))
			{
		    	handler.Migrate(conn, CreateBanListMigration.class);
			}
			
			// create warnings
			if( !tables.contains(WarningContract.TableWarnings.TABLE_NAME) )
			{
				handler.Migrate(conn, CreateWarningsMigration.class);
			}
			
		} 
    	catch (SQLException e) 
    	{
    		getLogger().severe("Database set-up failed.");
    		Bukkit.getPluginManager().disablePlugin(this);
			e.printStackTrace();
		}
    }
    
    /**
     * Returns a new connection source by fetching the given
     * database name's connection details from config
     * 
     * @param db	Name of the database in the config
     * @return
     */
    private ConnectionSource GetConnectionSource(String db)
    {
    	String connection = getConfig().getString("database." + db + ".connection");
    	String database = getConfig().getString("database." + db + ".database");
    	
    	String host = getConfig().getString("database.connections." + connection + ".host");
    	String port = getConfig().getString("database.connections." + connection + ".port");
    	String user = getConfig().getString("database.connections." + connection + ".user");
    	String pass = getConfig().getString("database.connections." + connection + ".pass");
    	
    	return new ConnectionSource(host, port, database, user, pass);
    }
    
    private void LoadConfig()
    {    	
    	getConfig().addDefault("server.maintenance", false);
    	//getConfig().addDefault("server.cache_bans", false);
    	
    	getConfig().addDefault("database.connections.local.host", "localhost");
    	getConfig().addDefault("database.connections.local.port", 3306);
    	getConfig().addDefault("database.connections.local.user", "root");
    	getConfig().addDefault("database.connections.local.pass", "");
    	getConfig().addDefault("database.connections.remote.host", "192.184.93.126");
    	getConfig().addDefault("database.connections.remote.port", 3306);
    	getConfig().addDefault("database.connections.remote.user", "root");
    	getConfig().addDefault("database.connections.remote.pass", "");
    	
    	getConfig().addDefault("database.banlist.database", "pcbridge");
    	getConfig().addDefault("database.banlist.connection", "remote");
    	getConfig().addDefault("database.warnings.database", "pcbridge");
    	getConfig().addDefault("database.warnings.connection", "remote");
    	
    	getConfig().options().copyDefaults(true);
    	saveConfig();
    }
    
}