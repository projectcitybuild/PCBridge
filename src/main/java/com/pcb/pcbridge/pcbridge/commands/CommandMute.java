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

import java.io.IOException;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.pcbridge.tasks.GetPlayerCacheTask;
import com.pcb.pcbridge.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandMute extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "mute";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Prevents a player from sending chat messages (does not prevent commands)";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.ban.mute";
	}

	@Override
	public String GetUsage()
	{
		return "/mute <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length != 1)
			return false;
				
		String alias = args.GetArg(0);
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
			.async(config -> {
				// check if this command was routed from /unmute
				boolean setToMute = args.GetData("UNMUTE") == null;
				
				// don't set a value if the value has not changed
				if(config.IsMuted == setToMute)
					return setToMute;
				
				System.out.println(config.IsMuted);
				config.IsMuted = setToMute;
				try
				{
					config.Save();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
				args.AddData("IS_DIRTY", true);
				return setToMute;
			})
			.syncLast(isMuted -> {
				
				// if no change occured, let the user know
				if(args.GetData("IS_DIRTY") == null)
				{
					String message = isMuted
							? ChatColor.RED + String.format("%s is already muted", alias)
							: ChatColor.RED + String.format("%s is not muted", alias);
					
					args.GetSender().sendMessage(message);
					return;
				}
				
				// otherwise broadcast their un/mute
				String message = isMuted
						? ChatColor.YELLOW + String.format("%s has been muted", alias)
						: ChatColor.YELLOW + String.format("%s has been unmuted", alias);					
					
				GetEnv().GetServer().broadcastMessage(message);		
				
			})
			.execute();
		
		return true;
	}

}
