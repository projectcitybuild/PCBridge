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
package com.pcb.pcbridge.archived.utils.cache;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.pcb.pcbridge.archived.utils.Actions.Action0;

/**
 * Cache type which removes data automatically after a given time
 *
 * @param <K>	Key
 * @param <V>	Value
 */
public class TimedCache<K, V> extends AbstractCache <K, V> {

	private final Plugin _plugin;
	private Map<K, CachedItem> _map = new HashMap<>();
	
	public TimedCache(Plugin plugin)
	{
		this._plugin = plugin;
	}
	
	@Override
	public V Get(K key)
	{
		CachedItem storage = _map.get(key);
		if(storage == null)
			return null;
		
		return storage.Value;
	}
	
	public void Remember(K key, V value, int seconds, Action0 callback)
	{
		// if key already exists, destroy the timer first
		if(_map.containsKey(key))
			StopTimer(key);
		
		int timerId = Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, () -> {
			Forget(key);
			
			if(callback != null)
				callback.Call();
		}, 20 * seconds);

		CachedItem item = new CachedItem(timerId, value);
		_map.put(key, item);
	}
	
	public void Remember(K key, V value, int seconds)
	{
		Remember(key, value, seconds, null);
	}

	@Override
	public void Forget(K key)
	{
		StopTimer(key);		
		_map.remove(key);
	}
	
	@Override
	public void Clear()
	{
		for(K key : _map.keySet())
		{
			StopTimer(key);
		}		
		
		_map.clear();
	}
	
	@Override
	public boolean ContainsKey(K key) 
	{
		return _map.containsKey(key);
	}
	
	@Override
	public boolean ContainsValue(V value) 
	{
		return _map.containsValue(value);
	}

	@Override
	public int Size() 
	{
		return _map.size();
	}
	
	/**
	 * Stops the given cache key's timer
	 * 
	 * @param key
	 */
	private void StopTimer(K key)
	{
		CachedItem item = _map.get(key);
		if(item == null)
			return;
		
		Bukkit.getScheduler().cancelTask(item.TimerId);
	}
	
	/**
	 * A data object to store in the cache instead of the value.
	 * It contains a reference to its timer id and the value.
	 */
	private class CachedItem
	{
		public final int TimerId;
		public final V Value;
		
		public CachedItem(int timerId, V value)
		{
			this.TimerId = timerId;
			this.Value = value;
		}
	}
	
}
