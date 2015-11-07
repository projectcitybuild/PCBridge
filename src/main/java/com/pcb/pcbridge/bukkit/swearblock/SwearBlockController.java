package com.pcb.pcbridge.bukkit.swearblock;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.bukkit.swearblock.commands.*;
import com.pcb.pcbridge.bukkit.swearblock.listeners.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;
import com.pcb.pcbridge.library.controllers.IListenerController;

/**
 * Controller: Handles any swear block related routing
 */
public class SwearBlockController extends AbstractController implements ICommandController, IListenerController
{
	public CommandRoute[] GetCommands()
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("swearblock", new CommandSwearBlock()),
		};
	}

	public Listener[] GetListeners() 
	{
		return new Listener[]
		{
			new ListenerOnPlayerChat()
		};
	}
}
