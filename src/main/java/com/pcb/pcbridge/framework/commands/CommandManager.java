package com.pcb.pcbridge.framework.commands;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles registration of commands and routes any
 * chat commands to its relevant handler
 */
public class CommandManager implements CommandExecutor, TabCompleter {

	private final WeakReference<Permission> permissionPlugin;
	private final Map<String, CommandRoute> commands = new HashMap<>();

	public CommandManager(Permission permission) {
		this.permissionPlugin = new WeakReference<>(permission);
	}

	public void registerCommands(JavaPlugin plugin, AbstractCommand[] commands) {
		for(AbstractCommand command : commands) {
			try {
				plugin.getCommand(command.getName())
					  .setExecutor(this);

			} catch(NullPointerException e) {
				// command failed to register, throw error
				// (probably not registered in plugin.yml)
			}

			CommandRoute route = new CommandRoute(command);
			for(String name : command.getAllNames()) {
				if(this.commands.containsKey(name)) {
					// throw error
				}
				this.commands.put(name, route);
			}
		}
	}

	public void unregister() {
		commands.clear();
	}

	/**
	 * Routes any chat command onto its respective handler
	 * 
	 * @param sender
	 * @param cmd
	 * @param label
	 * @param args
	 * 
	 * @return boolean	Whether the command is valid
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
		CommandRoute commandRoute = commands.get(cmd.getLabel());
		if(commandRoute == null) {
			return false;
		}

		Permission permission = this.permissionPlugin.get();
		if(permission == null) {
			sender.sendMessage(ChatColor.GRAY + "Permission check failed. Command could not be executed");
			return true;
		}

		AbstractCommand command = commandRoute.getCommand();
		if(!permission.has(sender, command.getPermissionNode())) {
			sender.sendMessage(ChatColor.RED + "You do not have access to that command");
			return true;
		}

		try {
			boolean isValidUsage = command.execute(sender, cmd, label, args);
//			if(!isValidUsage) {
//				sender.sendMessage();
//			}

			return isValidUsage;

		} catch(Exception e) {
			sender.sendMessage(ChatColor.GRAY + "An unhandled internal error occured");
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * Routes any [Tab] key events onto its respective handler
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) 
	{
	    CommandRoute route = commands.get(cmd.getLabel());
	    if(route == null) {
	        return null;
        }

        AbstractCommand command = route.getCommand();
        return command.tabComplete(sender, cmd, label, args);
	}
	
}
