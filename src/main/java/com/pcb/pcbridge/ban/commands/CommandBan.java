package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;

/**
 * Command: Ban the specified user (via username) from the server
 */

public final class CommandBan implements ICommand 
{	
	/**
	 * Determines if it's a permanent or temporary ban
	 */
	public boolean Execute(CommandPacket e, Object... args) 
	{
		if(e.Args.length == 0)
			return false;
		
		// if invoked via text input, it's a permanent ban
		if(args == null)
			return BanPlayer(e, false);
		
		// if invoked via code, it's a temporary ban
		return BanPlayer(e, (boolean) args[0]);
	}
	
	/**
	 * The actual ban logic lives here
	 * 
	 * @param e
	 * @param isTempBan
	 * @return
	 */
	private boolean BanPlayer(CommandPacket e, boolean isTempBan)
	{
		// get the current time as a unix timestamp
		Date currentDate = new Date();
		long now = currentDate.getTime() / 1000L;
		
		long expireDate = 0;
		if(isTempBan)
		{
			if(e.Args.length < 2)
				return false;
			
			String duration = e.Args[1];
			
			// ensure a numeric AND a time indicator is given
			String pattern = "^[0-9]+[a-z]+$";
			Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher m = r.matcher(duration);
			if(!m.find())
			{
				e.Sender.sendMessage(ChatColor.RED + "ERROR: Invalid ban duration given. Numeric AND a time indicator required (eg. 10h)");
				return false;
			}
			
			// determine ban length numeric
			int i = 0;
			while(Character.isDigit(duration.charAt(i))) i++;
			int length = Integer.parseInt( duration.substring(0, i) );
						
			// determine what time indicator was used
			String indicator = duration.substring(i, duration.length()).toLowerCase();
			Calendar c = Calendar.getInstance();
			switch(indicator)
			{
			case "m":
				c.add(Calendar.MINUTE, length);
				break;
			case "h":
				c.add(Calendar.HOUR, length);
				break;
			case "d":
				c.add(Calendar.HOUR, length * 24);
				break;
			case "w":
				c.add(Calendar.HOUR, length * 168);
				break;
			case "mo":
				c.add(Calendar.MONTH, length);				
				break;
			case "y":
				c.add(Calendar.YEAR, length);
				break;
			default:
				e.Sender.sendMessage(ChatColor.RED + "ERROR: Invalid time indicator.");
				return false;
			}
			
			expireDate = c.getTime().getTime() / 1000L;
		}
		
		
		String username = e.Args[0];
		PlayerUUID player = BanHelper.GetUUID(e.Plugin, username);
		
		// if given, stitch together the 'ban reason' which spans multiple args
		String banReason = "Griefing";
		int startIndex = isTempBan ? 2 : 1;
		if(e.Args.length > startIndex)
		{
			StringBuilder builder = new StringBuilder();
			for(int x=startIndex; x<e.Args.length; x++)
			{
				if(x > startIndex)
					builder.append(" ");
				
				builder.append(e.Args[x]);
			}
			banReason = builder.toString();
		}
		
		String staffName = e.Sender.getName();
		@SuppressWarnings("deprecation")
		String staffUUID = e.IsPlayer ? e.Plugin.getServer().getPlayer(staffName).getUniqueId().toString() : "";
		
		
		AbstractAdapter adapter = e.Plugin.GetAdapter();

		// check if the user is already banned
		boolean isBanned = false;
		try
		{
			isBanned = BanHelper.IsPlayerBanned(adapter, username, player.GetUUID());
		}
		catch(SQLException err)
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not look up player in ban records. Aborting");
			e.Plugin.getLogger().severe("Could not look up player in ban records: " + err.getMessage());
			return true;
		}
		
		if(isBanned)
		{
			e.Sender.sendMessage(ChatColor.GRAY + username + " is already banned.");
			return true;
		}
		
		// create the ban in storage
		try 
		{
			adapter.Execute("INSERT INTO pcban_active_bans(banned_name, banned_uuid, date_ban, date_expire, staff_uuid, staff_name, reason, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
					username, 
					player.GetUUID(), 
					now,
					isTempBan ? expireDate : null,
					staffUUID,
					staffName,
					banReason, 
					player.IP
			);
		} 
		catch (SQLException err) 
		{
			e.Sender.sendMessage(ChatColor.RED + "ERROR: Could not ban user. Please notify an administrator if this persists");
			e.Plugin.getLogger().severe("Could not add user to ban list: " + err.getMessage());
		}
		
		// kick the player if they're online
		if(player.IsOnline && player.Player != null)
		{
			String expiry = isTempBan ? new Date(expireDate * 1000L).toString() : "Never";
			String message = "Åòc" + "You have been banned.\n\n" +
					
						 "Åò8" + "Reason: Åòf" + banReason + "\n" +
						 "Åò8" + "Expires: Åòf" + expiry + "\n\n" + 
								 
						 "Åòb" + "Appeal @ www.projectcitybuild.com";
			
			player.Player.kickPlayer(message);
		}
		
		e.Sender.sendMessage(ChatColor.GRAY + username + " has been banned.");
		
		return true;
	}
}
