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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.archived.pcbridge.tasks.GetPlayerCacheTask;
import com.pcb.pcbridge.archived.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.archived.utils.commands.AbstractCommand;
import com.pcb.pcbridge.archived.utils.commands.CommandArgs;
import com.pcb.pcbridge.archived.utils.commands.CommandPrompt;
import com.pcb.pcbridge.archived.utils.MessageBuilder;

public class CommandTpLastPos extends AbstractCommand {
	
	@Override
	public String GetName() 
	{
		return "tplastpos";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Teleports to the last known position of an offline player";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.util.tplastpos";
	}

	@Override
	public String GetUsage()
	{
		return "/tplastpos <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{	
		if(!args.IsPlayer())
		{
			args.GetSender().sendMessage(ChatColor.RED + "Only players in-game can use this command.");
			return true;
		}
		
		if(args.GetArgs().length != 1)
			return false;
		
		// if confirming a tp, use the prompt data instead
		if(args.GetArg(0).equalsIgnoreCase("confirm"))
		{
			CommandPrompt prompt = GetPromptManager().GetPrompt("TPLASTPOS", args.GetSender());
			if(prompt != null)
			{
				prompt.Run();
				GetPromptManager().DestroyPrompt("TPLASTPOS", args.GetSender());
				return true;
			}
		}
		
		// check that the player isn't online
		String alias = args.GetArg(0);		
		for(Player player : GetEnv().GetServer().getOnlinePlayers())
		{
			if(player.getName().equalsIgnoreCase(alias))
			{
				args.GetSender().sendMessage(ChatColor.RED + "Cannot teleport to an online player.");
				return true;
			}
		}
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FetchUUID(alias))
			.async( uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
			.syncLast( config -> {
				if(config.LastPos == null)
				{
					args.GetSender().sendMessage(ChatColor.RED + "No last known position for " + alias);
					return;
				}
				
				args.GetSender().sendMessage(ChatColor.GRAY + "Last known position: " + config.LastPos);
				
				String promptMessage = new MessageBuilder()
					.Colour(ChatColor.AQUA)
					.String("Type ")
					.Colour(ChatColor.WHITE)
					.String("/tplastpos confirm")
					.Reset().Colour(ChatColor.AQUA)
					.String(" to teleport there")
					.Build();
				
				args.GetSender().sendMessage(promptMessage);
				
				GetPromptManager().Register("TPLASTPOS", args.GetSender(), () -> {
					args.GetSender().sendMessage(ChatColor.GRAY + "Teleporting...");
					args.GetPlayer().teleport(config.LastPos);
				});
			})
			.execute();
		
		return true;
	}

}
