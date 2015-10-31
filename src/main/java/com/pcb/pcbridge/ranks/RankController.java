package com.pcb.pcbridge.ranks;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.ranks.listeners.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.commands.CommandRoute;
import com.pcb.pcbridge.library.controllers.commands.ICommandController;
import com.pcb.pcbridge.library.controllers.listeners.IListenerController;

/**
 * Controller: Handles routing for forum rank linking
 */

public class RankController extends AbstractController implements IListenerController
{
	public Listener[] GetListeners() 
	{
		return new Listener[]
		{
			new ListenerOnPlayerLogin()
		};
	}
}
