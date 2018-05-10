package com.pcb.pcbridge;

import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.pcb.pcbridge.framework.database.DatabaseConnectionPool;
import com.pcb.pcbridge.framework.listeners.EventListener;
import com.pcb.pcbridge.framework.vault.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.pcb.pcbridge.framework.listeners.ListenerManager;

public final class PCBridge extends JavaPlugin {

	@Inject
	private VaultHook vault;

	private Optional<ListenerManager> listenerManager = Optional.empty();

	/**
	 * Logic to run everytime this plugin
	 * is enabled (ie. initial boot-up, reloads)
	 */
	@Override
    public void onEnable() {
		// manually force dependency injection since
		// we can't have Guice instantiate this file
		// for us at the beginning
		Injector injector = Guice.createInjector();
		injector.injectMembers(this);

		try {
			vault.hookIntoPermissionPlugin(this);
			vault.hookIntoChatPlugin(this);
		} catch(Exception e) {
			getLogger().severe(e.getLocalizedMessage());

			// this plugin cannot function properly without
			// the required plugin hooks working
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// register event listeners
		ListenerManager listenerManager = new ListenerManager(this);
		listenerManager.registerListeners(new EventListener[] {

		});
		this.listenerManager = Optional.of(listenerManager);
    }

	/**
	 * Logic to run everytime this plugin
	 * is disabled (ie. server shutdown, reloads)
	 */
	@Override
    public void onDisable() {
    	listenerManager.ifPresent(manager -> manager.unregisterAll());
    	listenerManager = Optional.empty();

		vault.unhookFromPermissionPlugin();
		vault.unhookFromChatPlugin();
		vault = null;
    }
    
    /**
     * Ensures all the required db tables exist and
     * runs the appropriate migrations if they don't
     */
//    private void prepareDatabase() {
//    	try(Connection conn = GetConnectionPool().GetConnection(BanListContract.DATABASE))
//    	{
//			MigrationHandler handler = new MigrationHandler(getLogger());
//
//			List<String> tables = new ArrayList<>();
//			try(ResultSet result = conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" }) )
//			{
//				while(result.next())
//				{
//					tables.add( result.getString("TABLE_NAME") );
//				}
//			}
//
//			// create player list
//			if( !tables.contains(PlayerContract.TablePlayers.TABLE_NAME) )
//			{
//		    	handler.Migrate(conn, CreatePlayersMigration.class);
//			}
//
//			// create ban list
//			if(!tables.contains(BanListContract.TableBans.TABLE_NAME)
//					|| !tables.contains(PlayerContract.TablePlayers.TABLE_NAME)
//					|| !tables.contains(BanListContract.TableUnbans.TABLE_NAME))
//			{
//		    	handler.Migrate(conn, CreateBanListMigration.class);
//			}
//
//			// create warnings
//			if( !tables.contains(WarningContract.TableWarnings.TABLE_NAME) )
//			{
//				handler.Migrate(conn, CreateWarningsMigration.class);
//			}
//
//		}
//    	catch (SQLException e)
//    	{
//    		getLogger().severe("Database set-up failed.");
//    		Bukkit.getPluginManager().disablePlugin(this);
//			e.printStackTrace();
//		}
//    }
    
//    private void loadConfig()
//    {
//    	getConfig().addDefault("server.maintenance", false);
//    	//getConfig().addDefault("server.cache_bans", false);
//
//    	getConfig().addDefault("database.connections.local.host", "localhost");
//    	getConfig().addDefault("database.connections.local.port", 3306);
//    	getConfig().addDefault("database.connections.local.user", "root");
//    	getConfig().addDefault("database.connections.local.pass", "");
//    	getConfig().addDefault("database.connections.remote.host", "192.184.93.126");
//    	getConfig().addDefault("database.connections.remote.port", 3306);
//    	getConfig().addDefault("database.connections.remote.user", "root");
//    	getConfig().addDefault("database.connections.remote.pass", "");
//
//    	getConfig().addDefault("database.banlist.database", "pcbridge");
//    	getConfig().addDefault("database.banlist.connection", "remote");
//    	getConfig().addDefault("database.warnings.database", "pcbridge");
//    	getConfig().addDefault("database.warnings.connection", "remote");
//
//    	getConfig().options().copyDefaults(true);
//    	saveConfig();
//    }
    
}