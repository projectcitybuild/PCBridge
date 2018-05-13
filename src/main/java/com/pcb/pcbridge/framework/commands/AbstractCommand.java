package com.pcb.pcbridge.framework.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractCommand {

	/**
	 * The string used to invoke the command
	 * (eg. 'ban')
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Gets a collection of alternative
	 * strings which can also invoke this
	 * command
	 *
	 * @return
	 */
	public String[] getAliases() {
		return new String[]{};
	}

	/**
	 * The permission string a player needs
	 * in order to invoke the command
	 *
	 * @return
	 */
	public abstract String getPermissionNode();

	/**
	 * Returns all names that can invoke
	 * this command, in a single set
	 *
	 * @return
	 */
	public Set<String> getAllNames() {
		Set<String> names = new HashSet<>();
		names.add(getName());

		for(String alias : getAliases()) {
			names.add(alias);
		}
		return names;
	}
	
	/**
	 * Logic to run when invoked
	 * 
	 * @param args
	 * @return
	 */
	public abstract boolean execute(CommandSender sender, Command cmd, String label, String[] args);
	
	/**
	 * Logic to run when the `Tab` key is pressed
	 * 
	 * @param args
	 * @return
	 */
	public List<String> tabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
