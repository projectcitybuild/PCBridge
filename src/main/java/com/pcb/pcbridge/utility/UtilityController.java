package com.pcb.pcbridge.utility;

import com.pcb.pcbridge.utility.commands.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.commands.CommandRoute;
import com.pcb.pcbridge.library.controllers.commands.ICommandController;

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
