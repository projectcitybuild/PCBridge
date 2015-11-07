package com.pcb.pcbridge.bukkit.players;

import org.bukkit.event.Listener;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.bukkit.players.listeners.*;
import com.pcb.pcbridge.library.controllers.AbstractController;
import com.pcb.pcbridge.library.controllers.IListenerController;

/**
 * Controller: Handles routing for the retrieval/creation of player settings
 */

public class PlayerController extends AbstractController implements IListenerController
{
	public Listener[] GetListeners() 
	{
		return new Listener[]
		{
			new ListenerOnPlayerLogin(),
			new ListenerOnPlayerLogout()
		};
	}
}
