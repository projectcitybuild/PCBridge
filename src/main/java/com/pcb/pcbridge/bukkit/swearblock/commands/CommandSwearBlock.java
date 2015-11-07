package com.pcb.pcbridge.bukkit.swearblock.commands;

import org.bukkit.entity.Player;

import com.pcb.pcbridge.bukkit.players.PlayerData;
import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;

/**
 * Command: Toggles on/off the swear filter per player
 */

public final class CommandSwearBlock extends AbstractCommand 
{	
	/**
	 * Toggle on/off the player's swear filter
	 */
	@Override
	public boolean Execute(CommandArgs e) 
	{
		// console cannot invoke this command
		if(!e.IsPlayer)
			return true;
		
		if(e.Args.length > 1)
			return false;
		
		Player player = (Player)e.Sender;
		PlayerData settings = _plugin.GetPlayerManager().GetSettings(player.getUniqueId());
		
		Boolean value;
		if(e.Args.length == 1)
		{
			// 'on/off' specified - use that
			value = StrToBool(e.Args[0]);
		}
		else
		{
			// no arg given - flip the current setting's value
			value = !settings.SwearFilter;
		}
		
		if(value == null)
			return false;
		
		settings.SwearFilter = value;
		
		_plugin.GetPlayerManager().SaveSettings(player.getUniqueId());
		MessageHelper.Send(MessageType.INFO, e.Sender, "Swear filter toggled " + BoolToStr(value));
		
		return true;
	}
	
	/**
	 * Converts 'on/off' string to boolean
	 * 
	 * @param str
	 * @return
	 */
	private Boolean StrToBool(String str)
	{
		str = str.toLowerCase();
		
		if(str == "on") return true;
		else if(str == "off") return false;
		else return null;
	}
	
	private String BoolToStr(boolean bool)
	{
		if(bool)
			return "on";
		else
			return "off";
	}
	
}
