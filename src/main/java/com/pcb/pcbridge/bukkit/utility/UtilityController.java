package com.pcb.pcbridge.bukkit.utility;

import com.pcb.pcbridge.bukkit.utility.commands.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;

/**
 * Controller: Handles any ban related routing
 */
public class UtilityController extends AbstractController implements ICommandController
{
	public CommandRoute[] GetCommands() 
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("pcbridge", 	new CommandPCBridge()),
		};
	}
}
