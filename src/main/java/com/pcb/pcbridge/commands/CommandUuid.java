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
package com.pcb.pcbridge.commands;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.json.simple.parser.ParseException;

import co.aikar.taskchain.TaskChain;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.tasks.AbortTask;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.UUIDFetcher;

public class CommandUuid extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "uuid";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Looks up a player's current UUID or name change history";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.uuid";
	}

	@Override
	public String GetUsage()
	{
		return "/uuid <name>  OR  /uuid history <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length > 2 || args.GetArgs().length == 0)
			return false;
		
		if(args.GetArgs().length == 1)
		{
			String alias = args.GetArg(0);
			GetCurrentUUID(args.GetSender(), alias);
		}
		else
		{
			if(!args.GetArg(0).equalsIgnoreCase("history"))
				return false;
				
			String alias = args.GetArg(1);
			GetNameHistory(args.GetSender(), alias);
		}
		
		return true;
	}
	
	/**
	 * Returns the given username's UUID
	 * 
	 * @param sender
	 * @param alias
	 */
	private void GetCurrentUUID(CommandSender sender, String alias)
	{
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.abortIfNull(AbortTask.Send(sender, ChatColor.RED + "Could not find UUID for %s. Does that player even exist?", alias))
			
			.syncLast(uuid -> {
				sender.sendMessage(alias + ": " + uuid);
			})
			.execute();
	}
	
	/**
	 * Returns the given username's name change history
	 * 
	 * @param sender
	 * @param alias
	 */
	private void GetNameHistory(CommandSender sender, String alias)
	{
		TaskChain<?> chain = PCBridge.NewChain();
		chain
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.abortIfNull(AbortTask.Send(sender, ChatColor.RED + "Could not find UUID for %s. Does that player even exist?", alias))
			
			.async(uuid -> {
				chain.setTaskData("UUID", uuid);

				UUIDFetcher fetcher = new UUIDFetcher();
				try 
				{
					return fetcher.GetNameHistory(uuid.toString());
				} 
				catch (ParseException | IOException e)
				{
					sender.sendMessage(ChatColor.RED + "Failed to lookup name history");
					e.printStackTrace();
					return null;
				}
			})
			
			.syncLast(history -> {
				MessageBuilder builder = new MessageBuilder()
					.Stringln("Name history of %s (%s)", alias, chain.getTaskData("UUID"))
					.Linebreak(2);
				
				Iterator<Entry<String, Long>> i = history.entrySet().iterator();
				while(i.hasNext())
				{
					Entry<String, Long> pair = i.next();
					
					builder.Stringln("Name: %s", pair.getKey());
					
					// only show the 'name change date' if it exists
					if(pair.getValue() != null)
					{
						Date date = new Date();
						date.setTime((long)pair.getValue());
						
						builder.Stringln("Changed to on: %s", GetEnv().GetDateFormatter().format(date));
					}					
					
					if(i.hasNext())
						builder.Stringln("---");
				}
			})
			.execute();
	}

}
