package com.pcb.pcbridge.bukkit.utility;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.bukkit.utility.listeners.*;
import com.pcb.pcbridge.bukkit.utility.commands.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;
import com.pcb.pcbridge.library.controllers.IListenerController;

/**
 * Controller: Handles any ban related routing
 */
public class UtilityController extends AbstractController implements ICommandController, IListenerController
{
	public CommandRoute[] GetCommands() 
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("pcbridge", new CommandPCBridge())
		};
	}

	public Listener[] GetListeners() 
	{
		return new Listener[]
		{
			new ListenerOnPlayerLogin()
		};
	}
}
