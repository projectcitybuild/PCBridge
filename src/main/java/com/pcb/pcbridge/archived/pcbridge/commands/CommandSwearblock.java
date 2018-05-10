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
package com.pcb.pcbridge.archived.pcbridge.commands;

import java.io.IOException;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.archived.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.archived.utils.commands.AbstractCommand;
import com.pcb.pcbridge.archived.utils.commands.CommandArgs;
import com.pcb.pcbridge.archived.utils.MessageBuilder;

public class CommandSwearblock extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "swearblock";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Toggles on/off your chat swear filter";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.chat.swearblock";
	}

	@Override
	public String GetUsage()
	{
		return "/swearblock <on|off>";
	}
	
	@Override
	public String[] GetAliases()
	{
		return new String[] { "swearfilter" };
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(!args.IsPlayer())
		{
			args.GetSender().sendMessage(ChatColor.RED + "Only players can use this command");
			return true;
		}
		
		if(args.GetArgs().length > 1)
			return false;
		
		if(args.GetArgs().length == 1)
		{
			if(!args.GetArg(0).equalsIgnoreCase("on") && !args.GetArg(0).equalsIgnoreCase("off"))
				return false;
		}
		
		PCBridge.NewChain()
			.asyncFirst( () -> {
				// fetch player config from cache
				PlayerConfig config = GetEnv().GetPlayerCache( args.GetPlayer().getUniqueId() );
				
				// 'on' or 'off'. if no value given then show the current value
				boolean setEnabled;
				if(args.GetArgs().length == 1)
				{
					if(args.GetArg(0).equalsIgnoreCase("on"))
						setEnabled = true;
					else
						setEnabled = false;
				}
				else
				{
					setEnabled = !config.IsSwearblockEnabled;
				}				
					
				config.IsSwearblockEnabled = setEnabled;
				
				try 
				{
					config.Save();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				return config.IsSwearblockEnabled;
			})
			.syncLast( isEnabled -> {
				
				String message;
				if(isEnabled)
					message = new MessageBuilder()
						.Colour(ChatColor.GRAY)
						.String("Swear filter is now enabled")
						.Build();
				else
					message = new MessageBuilder()
						.Colour(ChatColor.GRAY)
						.String("Swear filter is now disabled")
						.Build();
				
				args.GetSender().sendMessage(message);
			})
			.execute();
		
		return true;
	}

}
