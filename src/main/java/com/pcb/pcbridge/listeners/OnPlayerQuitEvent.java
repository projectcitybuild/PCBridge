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
package com.pcb.pcbridge.listeners;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.utils.cache.StaticCache;
import com.pcb.pcbridge.utils.listeners.AbstractListener;

public final class OnPlayerQuitEvent extends AbstractListener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnPlayerLogin(PlayerQuitEvent event)
	{		
		RemovePlayerCache(event.getPlayer());
	}
	
	/**
	 * Saves a player's last known position and then removes 
	 * their PlayerConfig from the cache
	 */
	private void RemovePlayerCache(Player player)
	{		
		StaticCache<UUID, PlayerConfig> cache = GetEnv().GetPlayerCache();
		if(cache != null)
		{
			PlayerConfig config = cache.Get(player.getUniqueId());
			config.LastPos = player.getLocation();
			try {
				config.Save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			cache.Forget(player.getUniqueId());
		}
	}
}