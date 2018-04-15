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
package com.pcb.pcbridge.pcbridge.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.pcbridge.tasks.GetPlayerCacheTask;
import com.pcb.pcbridge.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.utils.listeners.events.PlayerNameChangedEvent;
import com.pcb.pcbridge.utils.MessageBuilder;

public class CommandSuffix extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "suffix";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Manually adds a suffix to the given player.";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.chat.suffix";
	}

	@Override
	public String GetUsage()
	{
		return "/suffix <name> <prefix|off>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{	
		if(args.GetArgs().length < 2)
			return false;
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), args.GetArg(0)) )
			.abortIfNull()
			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
			.syncLast(config -> {
				
				StringBuilder builder = new StringBuilder();
				for(int i = 1; i < args.GetArgs().length; i++)
				{						
					builder.append(args.GetArg(i));

					if(i < (args.GetArgs().length - 1))
						builder.append(" ");
				}
				
				String suffix = builder.toString();				
				config.Suffix = suffix.equalsIgnoreCase("off") ? null : suffix;
				
				try 
				{
					config.Save();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
				if(suffix.equalsIgnoreCase("off"))
					suffix = "nothing";
				
				String message = new MessageBuilder()
					.Colour(ChatColor.GRAY)
					.String("%s had their suffix set to %s", args.GetArg(0), suffix)
					.Build();
				
				args.GetSender().sendMessage(message);
				
				// broadcast name change event
				Player player = GetEnv().GetServer().getPlayer(config.Uuid);
				GetEnv().BroadcastEvent( new PlayerNameChangedEvent(player) );
			})
			.execute();
		
		
		return true;
	}

}
