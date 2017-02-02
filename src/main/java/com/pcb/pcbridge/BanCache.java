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
package com.pcb.pcbridge;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.pcb.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.tasks.GetBansTask;
import com.pcb.pcbridge.utils.cache.StaticCache;

public class BanCache {

	// TODO: this is probably unnecessary unless the remote connection time is terrible...
	
	private final int CACHE_SEC_LIFETIME = 60 * 60 * 6;	// how often the cache refreshes its data
	
	private final StaticCache<String, PlayerBan> _bans;
	private BukkitTask _timer;
	
	public BanCache(JavaPlugin plugin)
	{
		this._bans = new StaticCache<>( new ConcurrentHashMap<>() );

		// creates a timer that rebuilds the list of active bans
		this._timer = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			
			plugin.getLogger().info("Refreshing active ban cache...");
			long start = System.nanoTime();
			
			_bans.Clear();
			GetBansTask.GetAllActive().forEach(ban -> {
				RememberBan(ban.PlayerUUID, ban);
			});
			
			long duration = (System.nanoTime() - start) / 1000000;
			plugin.getLogger().info("Ban cache rebuilt [ " + duration +  "ms ]");
			
		}, 0, CACHE_SEC_LIFETIME);
	}
	
	/**
	 * Stop the refresh timer empty the cache
	 */
	public void Destroy()
	{		
		if(_timer != null)
			_timer.cancel();
		
		_bans.Clear();
	}
	
	public PlayerBan GetBan(UUID uuid)
	{
		return _bans.Get( uuid.toString() );
	}
	
	public boolean IsBanned(UUID uuid)
	{
		return _bans.ContainsKey( uuid.toString() );
	}
	
	public void RememberBan(UUID uuid, PlayerBan ban)
	{
		_bans.Remember(uuid.toString(), ban);
	}
	
	public void RememberBan(String uuid, PlayerBan ban)
	{
		_bans.Remember(uuid, ban);
	}
	
}
