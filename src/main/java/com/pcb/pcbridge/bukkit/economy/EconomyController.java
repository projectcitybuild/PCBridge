package com.pcb.pcbridge.bukkit.economy;

import com.pcb.pcbridge.bukkit.economy.commands.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;

/**
 * Controller: Handles any ban related routing
 */
public class EconomyController extends AbstractController implements ICommandController
{
	public CommandRoute[] GetCommands() 
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("buyhead", new CommandBuyHead()),
		};
	}
}
