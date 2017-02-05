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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.pcb.pcbridge.Environment;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.tasks.GetBansTask;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.utils.commands.CommandPrompt;

public class CommandShowBans extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "showbans";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Retrieves all ban entries for the given name";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.ban.showbans";
	}

	@Override
	public String GetUsage() 
	{
		return "/showbans [name]";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{		
		// if a person used this command straight after /checkban (and no args given), 
		// then use data from the last /checkban instead
		if(args.GetArgs().length == 0)
		{
			CommandPrompt prompt = GetPromptManager().GetPrompt("CHECKBAN", args.GetSender());
			if(prompt != null)
			{
				List<PlayerBan> bans = (List<PlayerBan>) prompt.GetData("BANS");
				DisplayBans(bans, args);
				
				GetPromptManager().DestroyPrompt("CHECKBAN", args.GetSender());
				
				return true;
			}
			
			return false;
		}
		
		// otherwise use the first arg as a username and search for all bans applied to its UUID
		if(args.GetArgs().length > 1)
			return false;
		
		final String alias = args.GetArg(0);
		
		PCBridge.NewChain()
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.abortIfNull()
			
			.async( uuid -> GetBansTask.GetAllByUuid(uuid.toString(), false) )				
			.syncLast( bans -> DisplayBans(bans, args) )
			.execute();
		
		return true;
	}
	
	/**
	 * Displays the list of bans to the command sender
	 * 
	 * @param bans
	 * @param args
	 */
	private void DisplayBans(List<PlayerBan> bans, CommandArgs args)
	{
		MessageBuilder builder = new MessageBuilder();
		
		if(bans.size() == 0)
		{
			String message = builder.String("No bans on record")
					.Build();
			
			args.GetSender().sendMessage(message);
			return;
		}
		
		builder.Stringln("Showing ban records for %s (%s)");
		
		String uuid = null;
		String alias = (args.GetArgs().length == 0) ? null : args.GetArg(0);
		for(PlayerBan ban : bans)
		{		
			SimpleDateFormat formatter = Environment.DateFormat.Long();
			
			Date dtBan = new Date();
			dtBan.setTime((long)ban.Timestamp * 1000);			
			String dateBan = formatter.format(dtBan);
			
			String dateExpire = "Never";
			if(ban.ExpiryTimestamp != 0)
			{
				Date dtUnban = new Date();
				dtUnban.setTime((long)ban.ExpiryTimestamp * 1000);
				dateExpire = formatter.format(dtUnban);
			}			
			
			builder.Stringln("---------")
				.Stringln("Reason: %s", ban.Reason)
				.Stringln("Banned by: %s", ban.StaffAlias)
				.Stringln("Date: %s", dateBan)
				.Stringln("Expires: %s", dateExpire)
				.Linebreak();
			
			uuid = ban.PlayerUUID;
			
			if(alias == null)
				alias = ban.PlayerAlias;
		}

		args.GetSender().sendMessage( builder.Build(alias, uuid) );
	}

}
