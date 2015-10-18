package com.pcb.pcbridge.ban.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.AsyncAdapterParams;
import com.pcb.pcbridge.library.AsyncCallback;
import com.pcb.pcbridge.library.TimestampHelper;

/**
 * A special listener that invokes when PCBridge is enabled via onEnable()
 */

public class ListenerOnBoot 
{
	private final PCBridge _plugin;
	private int _queriesToRun;
	private int _queriesRun;
	private int _queriesSuceeded;
	
	public ListenerOnBoot(PCBridge plugin)
	{
		this._plugin = plugin;
	}
	
	/**
	 * Updates any expired temporary bans
	 * 
	 * @param plugin
	 */
	public void Execute()
	{
		_plugin.getLogger().info("Checking for any expired bans...");
		
		_plugin.GetAdapter().QueryAsync(
			new AsyncAdapterParams(
				"SELECT id,date_expire FROM pcban_active_bans WHERE is_active=1 and date_expire<=?", 
				TimestampHelper.GetNowTimestamp()
			),
			
			new AsyncCallback<List<HashMap<String, Object>>>() 
			{			
				@Override
				public void OnSuccess(List<HashMap<String, Object>> results) 
				{
					UpdateExpiredBans(results);
				}
						
				@Override
				public void OnError(Exception err) 
				{
					_plugin.getLogger().severe("Could not retrieve expired bans: " + err.getMessage());
				}
			}
		);
	}
	
	/**
	 * Goes through the list of expired bans and updates them in the "unban records" table
	 * 
	 * @param results
	 */
	private void UpdateExpiredBans(List<HashMap<String, Object>> results)
	{		
		if(results.size() == 0)
		{
			_plugin.getLogger().info("0 expired bans to update. Skipping step");
			return;
		}
		
		_plugin.GetAdapter().ExecuteAsync(
			new AsyncAdapterParams(
				"UPDATE pcban_active_bans SET is_active=0 WHERE date_expire<=?", 
				TimestampHelper.GetNowTimestamp()
			),
			
			new AsyncCallback<Object>() 
			{			
				@Override
				public void OnSuccess(Object results) { }
				
				@Override
				public void OnError(Exception err) 
				{
					// TODO: add to a list of errors and output the list at the end
				}
			}
		);
		
		_queriesToRun = results.size();
		_queriesRun = 0;
		
		final ListIterator<HashMap<String, Object>> i = results.listIterator();		
		while(i.hasNext())
		{
			HashMap<String, Object> ban = i.next();
			long banId = (long)ban.get("id");
			
			_plugin.GetAdapter().ExecuteAsync(
				new AsyncAdapterParams(
					"INSERT INTO pcban_unbans (ban_id, date) VALUES (?, ?)", 
					banId,
					(int)ban.get("date_expire")
				),
					
				new AsyncCallback<Object>() 
				{			
					@Override
					public void OnSuccess(Object results) 
					{
						_queriesSuceeded++;
						_queriesRun++;
						OutputToConsole();
					}
					
					@Override
					public void OnError(Exception err) 
					{
						_queriesRun++;
						OutputToConsole();
					}
				}
			);
		}
	}
	
	/**
	 * Checks if all records have been updated.
	 * Outputs to console if complete.
	 */
	private void OutputToConsole()
	{
		if(_queriesRun >= _queriesToRun)
		{
			_plugin.getLogger().info(_queriesSuceeded + " expired bans have been updated.");
			
			int failed = _queriesToRun - _queriesSuceeded;
			if(failed > 0)
				_plugin.getLogger().severe(failed + " expired bans failed to update");
		}
	}
}
