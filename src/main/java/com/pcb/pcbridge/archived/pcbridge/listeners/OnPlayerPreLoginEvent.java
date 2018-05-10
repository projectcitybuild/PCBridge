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

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import com.pcb.pcbridge.archived.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.archived.pcbridge.tasks.GetBansTask;
import com.pcb.pcbridge.archived.utils.MessageBuilder;
import com.pcb.pcbridge.archived.utils.MessageBuilder.RawColours;

public final class OnPlayerPreLoginEvent extends AbstractListener
{
	/**
	 * Called when a player first attempts to connect to the server.
	 * Nothing here will run on the main thread
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerPreLogin(AsyncPlayerPreLoginEvent event)
	{
		CheckIfBanned(event);
	}
	
	/**
	 * Prevents banned UUIDs from connecting to the server
	 * 
	 * @param event
	 */
	private void CheckIfBanned(AsyncPlayerPreLoginEvent event)
	{
		List<PlayerBan> bans = GetBansTask.GetAllByUuid(event.getUniqueId().toString(), true);
		
		for(PlayerBan ban : bans)
		{
			if(!ban.IsActive)
				continue;
			
			String message = new MessageBuilder()
				.Colour(MessageBuilder.RawColours.RED, MessageBuilder.RawFormats.BOLD)
				.String("You are currently banned.").Linebreak(2)
				.RawReset()
				.Colour(RawColours.GRAY)
				.String("Reason: ")
				.Colour(RawColours.WHITE)
				.Stringln(ban.Reason)	
				.Colour(RawColours.GRAY)
				.String("Expires: ")
				.Colour(RawColours.WHITE)
				.String(ban.GetTimeUntilExpiry()).Linebreak(2)
				.Colour(RawColours.AQUA)
				.String("Appeal @ www.projectcitybuild.com")
				.Build();
		
			event.disallow(Result.KICK_BANNED, message);
			break;
		}
	}
}
