package com.pcb.pcbridge.ban;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.ban.commands.*;
import com.pcb.pcbridge.ban.listeners.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.commands.CommandRoute;
import com.pcb.pcbridge.library.controllers.commands.ICommandController;
import com.pcb.pcbridge.library.controllers.listeners.IListenerController;

public class BanController extends AbstractController implements ICommandController, IListenerController
{
	public CommandRoute[] GetCommands() 
	{		
		return new CommandRoute[] 
		{ 
			new CommandRoute("ban", 		new CommandBan()),
			new CommandRoute("unban", 		new CommandUnban()),
			new CommandRoute("checkban", 	new CommandCheckBan()),
			new CommandRoute("tempban", 	new CommandTempBan()),
			new CommandRoute("banstats", 	new CommandBanStats()),
			new CommandRoute("lookup", 		new CommandLookup()),
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
