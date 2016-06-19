package com.pcb.pcbridge.bukkit.ban.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bukkit.scheduler.BukkitScheduler;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.bukkit.ban.Ban;

public class BanCache 
{
	private HashMap<String, List<Ban>> _cache = new HashMap<String, List<Ban>>();
	private Queue<BanQueueItem> _processQueue = new LinkedList<BanQueueItem>();
	private PCBridge _plugin;
	private BukkitScheduler _scheduler;
	
	public BanCache(PCBridge plugin)
	{
		_plugin = plugin;
		_scheduler = plugin.getServer().getScheduler();
	}
	

	/**
	 * Adds a ban to cache. Queues an operation if given
	 * 
	 * @param name 		Player's name
	 * @param entry		Ban data
	 * @param queueItem	Time intensive operation
	 */
	public void Remember(String name, Ban entry, BanQueueItem queueItem)
	{		
		List<Ban> entries = _cache.containsKey(name) ? 
				_cache.get(name) : new ArrayList<Ban>();
		
		entries.add(entry);
		_cache.put(name, entries);
		
		Enqueue(queueItem);
	}
	
	/**
	 * Replaces cache data with the given entries
	 * 
	 * @param name
	 * @param entries
	 * @param queueItem	Time intensive operation
	 */
	public void Set(String name, List<Ban> entries, BanQueueItem queueItem)
	{
		_cache.put(name, entries);
		
		Enqueue(queueItem);
	}
	
	/**
	 * Removes a ban from cache. Queues an operation if given
	 * 
	 * @param name 		Plyer's name
	 * @param entry		Ban data
	 * @param queueItem	Time intensive operation
	 */
	public void Forget(String name, BanQueueItem queueItem)
	{
		_cache.remove(name);
		Enqueue(queueItem);
	}
	
	/**
	 * Returns whether the cache contains an entry for the given name
	 * 
	 * @param name
	 * @return
	 */
	public Boolean Contains(String name)
	{
		return _cache.containsKey(name);
	}
	
	/**
	 * Retrieves a ban from cache by name.
	 * 
	 * @param name	Player's name
	 * @return
	 */
	public List<Ban> Get(String name)
	{
		return _cache.get(name);
	}
	
	
	/**
	 * Places an operation into the queue and triggers the Bukkit
	 * scheduler to run it if necessary
	 * 
	 * @param queueItem
	 */
	private void Enqueue(BanQueueItem queueItem)
	{
		if(queueItem == null)
			return;
		
		_processQueue.add(queueItem);
		
		if(_processQueue.size() == 1)
			ProcessQueue();
	}
	
	
	/**
	 * Processes the operation queue. Triggers the Bukkit scheduler 
	 * to run the next operation in the queue if one exists
	 */
	private void ProcessQueue()
	{
		if(_processQueue.size() == 0)
			return;
		
		_scheduler.scheduleSyncDelayedTask(_plugin, new Runnable() {
            @Override
            public void run() 
            {
            	BanQueueItem item = _processQueue.remove();
        		item.OnProcess();
        		
        		if(_processQueue.size() > 0)
        			ProcessQueue();
            }
        }, 20L);
	}

	
}
