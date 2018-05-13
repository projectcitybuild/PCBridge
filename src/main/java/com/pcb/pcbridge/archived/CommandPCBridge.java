//package com.pcb.pcbridge.archived;
//
//import net.md_5.bungee.api.ChatColor;
//
//import org.spigot.command.CommandSender;
//import org.spigot.plugin.Plugin;
//import org.spigot.plugin.PluginManager;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
////import com.pcb.pcbridge.utils.database.migrations.BanListV2Migration;
//
//
//public class CommandPCBridge extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "pcbridge";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Various PCBridge utility commands";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.admin";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/pcbridge <key> <value>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length < 1)
//			return false;
//
//		switch(args.GetArg(0).toLowerCase())
//		{
//			case "maintenance":
//				return SetMaintenanceMode(args);
//			case "migrate":
//				return RunMigration(args);
//			case "reloadconfig":
//				return ReloadConfig(args.GetSender());
//			case "reload":
//				return ReloadPlugin(args.GetSender());
//			case "version":
//				return GetVersion(args.GetSender());
//			default:
//				args.GetSender().sendMessage("Invalid key");
//				return false;
//		}
//	}
//
//	private boolean SetMaintenanceMode(CommandArgs args)
//	{
//		boolean isEnabled = GetEnv().GetConfig().getBoolean("server.maintenance");
//
//		if(args.GetArgs().length == 1)
//		{
//			args.GetSender().sendMessage("Maintenance mode is currently " + BoolToString(isEnabled) );
//			return true;
//		}
//
//		String value = args.GetArg(1);
//		if(args.GetArgs().length != 2 || (!value.equalsIgnoreCase("on") && !value.equalsIgnoreCase("off")) )
//		{
//			args.GetSender().sendMessage(ChatColor.GRAY + "/pcbridge maintenance <on|off>");
//			return true;
//		}
//
//		Boolean newValue = value.equalsIgnoreCase("on") ? true : false;
//
//		GetEnv().GetConfig().set("server.maintenance", newValue);
//		GetEnv().GetPlugin().saveConfig();
//
//		args.GetSender().sendMessage("Maintenance mode is now " + BoolToString(newValue) );
//
//		/*int timerId = GetEnv().GetServer().getScheduler().scheduleSyncRepeatingTask(GetEnv().GetPlugin(), () -> {
//			GetEnv().GetServer().broadcastMessage(ChatColor.YELLOW + "-- Server is currently in Maintenance Mode --");
//		}, 0, 20 * 120);*/
//
//		return true;
//	}
//
//	private String BoolToString(boolean value)
//	{
//		return value ? "ON" : "OFF";
//	}
//
//	/**
//	 * Runs the given database migration off the main thread
//	 *
//	 * @param args
//	 * @return
//	 */
//	private boolean RunMigration(CommandArgs args)
//	{
//		if(args.GetArgs().length != 2)
//			return false;
//
//		Class<? extends IMigrate> migration = null;
//		String db = null;
//		switch(args.GetArg(1).toLowerCase())
//		{
//			// disabled - no need to run this migration anymore
//
//			//case "banlist_v2":
//			//	migration = BanListV2Migration.class;
//			//	db = BanListContract.DATABASE;
//			//	break;
//			case "warnings_v2":
//				migration = WarningsV2Migration.class;
//				db = BanListContract.DATABASE;
//				break;
//			default:
//				args.GetSender().sendMessage(ChatColor.RED + "Invalid migration name");
//				return true;
//		}
//
//		MigrationHandler handler = new MigrationHandler( GetEnv().GetLogger() );
//
//		long start = System.nanoTime();
//
//		final Class<? extends IMigrate> fMigration = migration;
//		final String fDb = db;
//		PCBridge.NewChain()
//			.async( () -> handler.Migrate(fMigration, fDb) )
//			.sync( () -> {
//				long duration = (System.nanoTime() - start) / 1000000;
//				String message = String.format(ChatColor.GREEN + "Migration complete [%sms]", duration);
//				args.GetSender().sendMessage(message);
//			} )
//			.execute();
//
//		return true;
//	}
//
//	/**
//	 * Reloads only the config file
//	 *
//	 * @param sender
//	 * @return
//	 */
//	private boolean ReloadConfig(CommandSender sender)
//	{
//		GetEnv().GetPlugin().reloadConfig();
//		GetEnv().GetLogger().info("Config reloaded.");
//		sender.sendMessage("Config reloaded");
//
//		return true;
//	}
//
//	/**
//	 * Reloads the entire plugin
//	 *
//	 * @param sender
//	 * @return
//	 */
//	private boolean ReloadPlugin(CommandSender sender)
//	{
//		GetEnv().GetLogger().info("Attempting to reload plugin...");
//
//		Plugin plugin = GetEnv().GetPlugin();
//		PluginManager pm = GetEnv().GetServer().getPluginManager();
//		pm.disablePlugin(plugin);
//		pm.enablePlugin(plugin);
//
//		GetEnv().GetLogger().info("Plugin reloaded.");
//		sender.sendMessage("Plugin reloaded");
//
//		return true;
//	}
//
//	/**
//	 * Prints out this plugin's version
//	 *
//	 * @param sender
//	 * @return
//	 */
//	private boolean GetVersion(CommandSender sender)
//	{
//		sender.sendMessage("PCBridge: Version " + GetEnv().GetPlugin().getDescription().getVersion());
//
//		return true;
//	}
//
//}
