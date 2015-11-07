package com.pcb.pcbridge.bukkit.ranks;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.bukkit.ranks.listeners.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.CommandRoute;
import com.pcb.pcbridge.library.controllers.ICommandController;
import com.pcb.pcbridge.library.controllers.IListenerController;

/**
 * Controller: Handles routing for forum rank synchronisation
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
