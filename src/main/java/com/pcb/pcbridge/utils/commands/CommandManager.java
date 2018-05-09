/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.utils.commands;

import java.io.InvalidClassException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.utils.commands.CommandPromptManager;
import com.pcb.pcbridge.utils.commands.ProxyCommand;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.pcb.pcbridge.Environment;

/**
 * Handles registration of commands and routes any
 * chat commands to its relevant handler
 */
public class CommandManager implements CommandExecutor, TabCompleter {



	private final Environment _env;
	private final Permission _permissions;
	private final CommandPromptManager _promptManager;
	private Map<String, AbstractCommand> _commands = new HashMap<>();
	private CommandMap _commandMap;
	
	public CommandPromptManager GetPromptManager()
	{
		return _promptManager;
	}
	
	public CommandManager(Environment environment, Permission permissions, AbstractCommand[] commands)
	{		
		this._env = environment;
		this._permissions = permissions;
		this._promptManager = new CommandPromptManager(environment);
		
		for(AbstractCommand command : commands)
		{
			command.SetEnv(environment);
			command.SetCommandManager(this);
			
			RegisterCommand(command);
		}
	}

	/**
	 * Registers this class to be the executor of the given command
	 * and stores a reference to it
	 * 
	 * @param command	Command to be registered
	 */
	private void RegisterCommand(AbstractCommand command)
	{		
		// ProxyCommand inherits a Bukkit command, but to cut down on the bloat
		// we set its executor to be this class, and then route any calls to an
		// AbstractCommand - which contains all the command info and logic
		ProxyCommand proxy = new ProxyCommand( command.GetName() );
		proxy.setDescription( command.GetDescription() );
		proxy.setUsage( command.GetUsage() );
		proxy.setPermission( command.GetPermission() );
		proxy.setPermissionMessage("You do not have permission to use this command.");
		
		if(command.GetAliases() != null)
			proxy.setAliases( Arrays.asList(command.GetAliases()) );
		
	    // through some reflection magic, we can register this command
	    // without having to write anything in plugin.yml
	    try 
	    {	    	
	    	proxy.SetExecutor(this);
		    GetCommandMap().register(command.GetName(), "", proxy);
		    
		    _commands.put(command.GetName(), command);
		    
		    // also register the fallback command label which is used when command 
		    // name conflicts occur
		    _commands.put("pcbridge:" + command.GetName(), command);
		} 
	    catch (InvalidClassException e) 
	    {
			_env.GetLogger().severe(e.getMessage());
		}
	    
	    // if Essentials or another plugin implements a command with the same name,
	    // sadly we'll still need to specify it in plugin.yml in order to override it
	    try
	    {
		    _env.GetPlugin().getCommand( command.GetName() ).setExecutor(this);
	    }
	    catch(NullPointerException e)
	    {
	    	// do nothing
	    }
	}
	
	/**
	 * Unregisters every command related to this plugin
	 */
	public void UnregisterAll()
	{
		for(AbstractCommand command : _commands.values())
		{
			CommandMap cMap = GetCommandMap();
			Command proxy = cMap.getCommand(command.GetName());
			
			if(proxy != null)
				proxy.unregister(cMap);
			else
				_env.GetLogger().warning("Failed to unregister command [" + command.GetName() + "]");
		}
	}
	
	/**
	 * Manually runs the given command in code as if it were called via chat command.
	 * Used when you want to run a command from within another command
	 * 
	 * @param command	Command to run
	 * @param args		The CommandArgs passed to the first command
	 * @return
	 */
	public boolean InvokeCommand(String command, CommandArgs args)
	{
		AbstractCommand handler = _commands.get(command);
		if(handler != null)
		{
			return handler.OnExecute(args);
		}
		
		return false;
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		//_env.GetLogger().info("Call to: " + cmd.getLabel());
		
		AbstractCommand handler = _commands.get(cmd.getLabel());
		if(handler == null)
			return false;
		
		// check player has permission to use the command
		if(!_permissions.has(sender, handler.GetPermission()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
			return true;
		}
		
		CommandArgs commandArgs = new CommandArgs(sender, cmd, label, args);
		try
		{
			boolean isValid = handler.OnExecute(commandArgs);
			if(!isValid)
				sender.sendMessage(handler.GetUsage());
			
			return isValid;
		}
		catch(Exception e)
		{
			// catch any exceptions that bubbled up
			sender.sendMessage("An unhandled internal error occured.");
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
		AbstractCommand handler = _commands.get(cmd.getLabel());
		if(handler != null)
		{
			CommandArgs commandArgs = new CommandArgs(sender, cmd, label, args);
			return handler.OnTabComplete(commandArgs);
		}
		
		return null;
	}
	
	/**
	 * Returns a reference to Bukkit's 'Command Map' which lets us
	 * manually register commands at runtime
	 * 
	 * @return
	 */
	private final CommandMap GetCommandMap() 
	{
		if(_commandMap != null)
			return _commandMap;
		
		try
		{
			Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);
			_commandMap = (CommandMap) f.get(Bukkit.getServer());
			return GetCommandMap();
		}
           catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) 
		{
			e.printStackTrace();
		}			
       
        return null;
	}
		
}
