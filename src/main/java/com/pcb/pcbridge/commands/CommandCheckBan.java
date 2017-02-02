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
import java.util.UUID;

import org.bukkit.ChatColor;

import co.aikar.taskchain.TaskChain;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.models.PlayerBan;
import com.pcb.pcbridge.tasks.AbortTask;
import com.pcb.pcbridge.tasks.GetBansTask;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandCheckBan extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "checkban";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Checks if a player is currently banned from PCB";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.ban.checkban";
	}

	@Override
	public String GetUsage() 
	{
		return "/checkban <name>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(args.GetArgs().length != 1)
			return false;

		final String alias = args.GetArg(0);
		TaskChain<?> chain = PCBridge.NewChain();
		chain
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), alias) )
			.abortIfNull( AbortTask.Send(args.GetSender(), ChatColor.GRAY + "Cannot find UUID for %s. Does that player even exist?", alias) )
			
			.async(uuid -> {
				chain.setTaskData("UUID", uuid);
				return GetBansTask.GetAllByUuid(uuid.toString(), false); 
			})
			
			.syncLast( bans -> DisplayBans(bans, args, chain.getTaskData("UUID")) )
			.execute();
		
		return true;
	}
	
	private void DisplayBans(List<PlayerBan> bans, CommandArgs args, UUID uuid)
	{
		// check if the player is banned first
		boolean isBanned = false;
		PlayerBan banEntry = null;
		for(PlayerBan ban : bans)
		{
			if(ban.IsActive)
			{
				isBanned = true;
				banEntry = ban;
				break;
			}
		}
		
		MessageBuilder builder = new MessageBuilder();
		if(isBanned)
		{
			SimpleDateFormat formatter = GetEnv().GetDateFormatter();
			
			Date dtBan = new Date();
			dtBan.setTime((long)banEntry.Timestamp * 1000);
			String dateBan = formatter.format(dtBan);
			
			String dateExpire = "Never";
			if(banEntry.ExpiryTimestamp != 0)
			{
				Date dtUnban = new Date();
				dtUnban.setTime((long)banEntry.ExpiryTimestamp * 1000);
				dateExpire = formatter.format(dtUnban);
			}		
			
			builder.Stringln("%s is currently banned (%s)", banEntry.PlayerAlias, uuid.toString())
				.Stringln("---")
				.Stringln("Reason: %s", banEntry.Reason)
				.Stringln("---")
				.Stringln("Banned by: %s", banEntry.StaffAlias)
				.Stringln("Date: %s", dateBan)
				.Stringln("Expires: %s", dateExpire);
		}			
		else
		{
			builder.Colour(ChatColor.GREEN)
				.Stringln("%s is not currently banned", args.GetArg(0))
				.Colour(ChatColor.WHITE)
				.Stringln(uuid.toString());
		}
		
		// display number of bans on record if 'not banned' or 'banned and has more than 1 ban'
		if((!isBanned && bans.size() > 0) || (isBanned && bans.size() > 1))
		{
			int count = bans.size();
			if(isBanned)
			{
				builder.Stringln("---")
					.Stringln("%s other bans on record. Type /showbans to see view them", Integer.toString(count - 1))
					.Stringln("---");
			}
			else
			{
				builder.Stringln("---")
					.Stringln("%s bans on record. Type /showbans to see view them", Integer.toString(count))
					.Stringln("---");
			}
			
			// create a prompt so the user can use /showbans
			GetPromptManager().Register("CHECKBAN", args.GetSender())
				.SetData("BANS", bans);
		}
		
		args.GetSender().sendMessage( builder.Build() );
		
	}

}
