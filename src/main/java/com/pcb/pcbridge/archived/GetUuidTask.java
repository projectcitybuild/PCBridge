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
package com.pcb.pcbridge.archived;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

public class GetUuidTask {
	
	/**
	 * Gets the UUID of the given name,
	 * but check online players first (as that's always faster than a HTTP request)
	 */
	public static UUID FindOrFetch(Server server, String username)
	{	
		// check if online first
		for(Player player : server.getOnlinePlayers())
		{
			if(player.getName().equalsIgnoreCase(username))
				return player.getUniqueId();
		}
		
		// check offline player cache (usercache.json - which holds user data for 30 days)
		@SuppressWarnings("deprecation")
		OfflinePlayer offPlayer = server.getOfflinePlayer(username);		
		if(offPlayer != null && offPlayer.hasPlayedBefore())
			return offPlayer.getUniqueId();
		
		// as a last resort, search them up via the Mojang web API
		return FetchUUID(username);
	}
	
	/**
	 * Get the UUID via the Mojang web API
	 * 
	 * @param username
	 * @return
	 */
	public static UUID FetchUUID(String username)
	{
		try 
		{
			UUID uuid = new UUIDFetcher().GetCurrentUUID(username);			
			return uuid;
		} 
		catch (IOException | ParseException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
