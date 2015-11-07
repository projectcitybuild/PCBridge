package com.pcb.pcbridge.bukkit.ban;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.bukkit.ban.listeners.*;
import com.pcb.pcbridge.bukkit.ban.commands.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;
import com.pcb.pcbridge.library.controllers.IListenerController;

/**
 * Controller: Handles any ban related routing
 */
public class BanController extends AbstractController implements ICommandController, IListenerController
{
	public CommandRoute[] GetCommands() 
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("ban", 		new CommandBan()),
			new CommandRoute("unban", 		new CommandUnban()),
			new CommandRoute("lookup", 		new CommandLookup()),
			new CommandRoute("tempban", 	new CommandTempBan()),
			new CommandRoute("uuid", 		new CommandUUID()),
		};
	}

	public Listener[] GetListeners() 
	{
		return new Listener[]
		{
			new ListenerOnPlayerLogin()
		};
	}
	
	@Override
	public void OnBoot(PCBridge plugin)
	{
		new ListenerOnBoot(plugin).Execute();
	}
}
