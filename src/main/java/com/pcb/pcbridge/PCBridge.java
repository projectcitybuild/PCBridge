package com.pcb.pcbridge;

import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.pcb.pcbridge.features.config.ConfigManager;
import com.pcb.pcbridge.framework.commands.AbstractCommand;
import com.pcb.pcbridge.framework.commands.CommandManager;
import com.pcb.pcbridge.framework.listeners.EventListener;
import com.pcb.pcbridge.framework.vault.ChatHookFailedException;
import com.pcb.pcbridge.framework.vault.PermissionHookFailedException;
import com.pcb.pcbridge.framework.vault.VaultHook;
import com.pcb.pcbridge.spigot.chat.commands.CommandMute;
import com.pcb.pcbridge.spigot.maintenance.commands.CommandMaintenance;
import com.pcb.pcbridge.spigot.maintenance.listeners.ListenerMaintenanceLogin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.pcb.pcbridge.framework.listeners.ListenerManager;

public final class PCBridge extends JavaPlugin {

	@Inject
	private VaultHook vault;

	@Inject
	private ConfigManager configManager;

	private Optional<ListenerManager> listenerManager = Optional.empty();
	private Optional<CommandManager> commandManager = Optional.empty();

	/**
	 * Logic to run every time this plugin
	 * is enabled (ie. initial boot-up, reloads)
	 */
	@Override
    public void onEnable() {
		// manually force dependency injection since
		// we can't have Guice instantiate this file
		// for us at the beginning
		BaseContainer container = new BaseContainer(this);
		Injector injector = Guice.createInjector(container);
		injector.injectMembers(this);

		try {
			vault.hookIntoPermissionPlugin(this);
			vault.hookIntoChatPlugin(this);
		} catch(PermissionHookFailedException | ChatHookFailedException e) {
			getLogger().severe(e.getLocalizedMessage());
			e.printStackTrace();

			// this plugin cannot function properly without
			// the required plugin hooks working
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		configManager.load();

		// register event listeners
		ListenerManager listenerManager = new ListenerManager(this);
		listenerManager.registerListeners(new EventListener[] {
                injector.getInstance(ListenerMaintenanceLogin.class),
		});
		this.listenerManager = Optional.of(listenerManager);

		// register commands
		Optional<Permission> permission = vault.getPermission();
		if(!permission.isPresent()) {
			getLogger().severe("No permission plugin instance available for command registration");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		CommandManager commandManager = new CommandManager(permission.get());
		commandManager.registerCommands(this, new AbstractCommand[] {
				new CommandMute(),
                injector.getInstance(CommandMaintenance.class),
		});
		this.commandManager = Optional.of(commandManager);
    }

	/**
	 * Logic to run every time this plugin
	 * is disabled (ie. server shutdown, reloads)
	 */
	@Override
    public void onDisable() {
    	listenerManager.ifPresent(manager -> manager.unregisterAll());
    	listenerManager = Optional.empty();

    	commandManager.ifPresent(manager -> manager.unregister());
    	commandManager = Optional.empty();

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
    
}