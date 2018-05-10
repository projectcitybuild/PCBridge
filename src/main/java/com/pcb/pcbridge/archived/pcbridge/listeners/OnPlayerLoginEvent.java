/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.archived.pcbridge.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.pcb.pcbridge.archived.utils.MessageBuilder;
import com.pcb.pcbridge.archived.utils.MessageBuilder.RawColours;

public final class OnPlayerLoginEvent extends AbstractListener
{
	/**
	 * Called after a player has established a connection to the server
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerPreLogin(PlayerLoginEvent event)
	{
		CheckIfMaintenance(event);
	}
		
	/**
	 * Prevents users from joining if the server is in maintenance mode
	 * 
	 * @param event
	 */
	private void CheckIfMaintenance(PlayerLoginEvent event)
	{
		if(GetEnv().GetConfig().getBoolean("server.maintenance"))
		{
			if(!event.getPlayer().hasPermission("pcbridge.maintenance.access"))
			{
				String message = new MessageBuilder()
					.Colour(RawColours.AQUA)
					.String("Server maintenance in progress.")
					.Linebreak(2)
					.Colour(RawColours.WHITE)
					.String("Visit www.projectcitybuild.com for details.")
					.Build();
				
				event.disallow(Result.KICK_WHITELIST, message);
			}
		}
	}
}
