package com.pcb.pcbridge.bukkit.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.ban.BanHelper;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.async.IFutureCallback;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Command: Unbans the specified player
 */

public final class CommandUnban extends AbstractCommand 
{	
	public boolean Execute(final CommandArgs e) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		/*BanHelper.GetUUIDAsync(e.Plugin, e.Args[0], new IFutureCallback<PlayerUUID>() 
		{			
			@Override
			public void OnSuccess(PlayerUUID player) 
			{
				LookupPlayer(e, player);
			}
			
			@Override
			public void OnError(Exception err) 
			{
				e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not determine player's UUID.");
				e.Plugin.getLogger().severe("Mojang API query for UUID failed: " + err.getMessage());			
			}
		});*/
		
		return true;
	}

	/**
	 * Checks if the given player is currently banned
	 * 
	 * @param player
	 */
	private void LookupPlayer(final CommandArgs e, final PlayerUUID player)
	{
		/*// retrieve ban from storage
		AbstractAdapter adapter = e.Plugin.GetAdapter();
				
		AsyncAdapterParams params = BanHelper.GetLookupSQL(adapter, e.Args[0], player.GetUUID());
		adapter.QueryAsync(params, new IFutureCallback<List<HashMap<String, Object>>>() 
		{
			@Override
			public void OnSuccess(List<HashMap<String, Object>> records) 
			{
				boolean isBanned = (records != null && records.size() > 0);			
				if(!isBanned)
				{
					e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " is not currently banned.");
				}
				else
				{
					UnbanPlayer(e, player, records);
				}
			}

			@Override
			public void OnError(Exception err) 
			{
				e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not lookup player in ban records.");
				e.Plugin.getLogger().severe("Could not lookup player in ban records: " + err.getMessage());			
			}
		});		*/
	}
	
	private void UnbanPlayer(final CommandArgs e, PlayerUUID player, List<HashMap<String, Object>> records)
	{
		/*AbstractAdapter adapter = e.Plugin.GetAdapter();
		
		// unban the user but keep a record of who unbanned and when
		String staffUUID = null;
		if(e.IsPlayer)
		{
			@SuppressWarnings("deprecation")
			Player staff = e.Plugin.getServer().getPlayer(e.Sender.getName());
			staffUUID = staff.getUniqueId().toString();
		}
						
		ListIterator<HashMap<String, Object>> i = records.listIterator();
		while(i.hasNext())
		{
			HashMap<String, Object> record = i.next();
			long id = (long) record.get("id");
							
			try
			{
				adapter.Execute("UPDATE pcban_active_bans SET is_active=0 WHERE id=?", id);
								
				adapter.Execute(
					new QueryBuilder().Insert("pcban_unbans")
						.Field("ban_id", id)
						.Field("staff_uuid", staffUUID)
						.Field("date", TimestampHelper.GetNowTimestamp())
						.Build()
				);
			} 
			catch (SQLException err) 
			{
				e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not unban player.");
				e.Plugin.getLogger().severe("Could not unban player: " + err.getMessage());
			}
		}		
						
		e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " has been unbanned.");*/
	}
}
