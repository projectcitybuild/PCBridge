package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.controllers.commands.CommandArgs;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;
import com.pcb.pcbridge.library.database.querybuilder.QueryBuilder;

/**
 * Command: Unbans the specified player
 */

public final class CommandUnban implements ICommand 
{	
	public boolean Execute(CommandArgs e, Object... args) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		String username = e.Args[0];
		PlayerUUID player = BanHelper.GetUUID(e.Plugin, username);
		
		String staffUUID = null;
		if(e.IsPlayer)
		{
			PlayerUUID staff = BanHelper.GetUUID(e.Plugin, e.Sender.getName());
			staffUUID = staff.GetUUID();
		}
			
		// retrieve ban from storage
		AbstractAdapter adapter = e.Plugin.GetAdapter();
		
		List<HashMap<String, Object>> records;
		boolean isBanned;
		try
		{
			records = BanHelper.LookupPlayer(adapter, username, player.GetUUID());
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not lookup player in ban records.");
			e.Plugin.getLogger().severe("Could not lookup player in ban records: " + err.getMessage());
			return true;
		}
		
		isBanned = (records != null && records.size() > 0);
				
		if(!isBanned)
		{
			e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " is not currently banned.");
			return true;
		}
		
		// unban the user but keep a record of who unbanned and when
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
				return true;
			}
		}		
		
		e.Sender.sendMessage(ChatColor.AQUA + e.Args[0] + ChatColor.WHITE + " has been unbanned.");
		return true;
	}
}
