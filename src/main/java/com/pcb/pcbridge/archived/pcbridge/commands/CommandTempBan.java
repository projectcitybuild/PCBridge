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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pcb.pcbridge.archived.utils.commands.AbstractCommand;
import com.pcb.pcbridge.archived.utils.commands.CommandArgs;

public class CommandTempBan extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "tempban";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Bans a player from PCB for only the given duration";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.ban.tempban";
	}

	@Override
	public String GetUsage() 
	{
		return "/tempban <name> <0-9><w|d|h|m> [reason]";
	}

	@Override
	public boolean OnExecute(CommandArgs args)
	{
		if(args.GetArgs().length < 2)
			return false;
		
		long duration = GetBanLength(args.GetArg(1));
		if(duration == -1)
		{
			args.GetSender().sendMessage("Invalid ban duration. Correct: <number>[m|h|d|w] (eg. 3w or 1h30m)");
			return false;
		}
		
		args.AddData("BAN_LENGTH", duration);
		
		// route the command to the 'ban' command
		return GetCommandManager().InvokeCommand("ban", args);
	}
	
	/**
	 * Converts a string of format [0-9][m|h|d|w] to a UNIX timestamp into the future.
	 * Returns -1 if invalid format
	 * 
	 * @param string
	 * @return
	 */
	public long GetBanLength(String string)
	{
		// add each pattern match to a map so that duplicates (eg. 2h|2h) are discarded
		HashMap<String, Integer> times = new HashMap<String, Integer>();
		Matcher matcher = Pattern.compile("([0-9]+[a-zA-Z])").matcher(string);
		
		int x = 0;				
		while(matcher.find()) 
		{
			String caught = matcher.group();
			
			// split numbers from letters
			Integer numbers = null;
			String letter = null;
			Matcher m = Pattern.compile("[0-9]+|[a-zA-Z]").matcher(caught);
			while (m.find()) 
			{
				String match = m.group();
				try 
				{
					int numeric = Integer.parseInt(match);
					numbers = numeric;
				}
				catch (NumberFormatException e) 
				{
					letter = match;
				}
			}
			
			times.put(letter, numbers);
			x++;
        }
		
		if(x == 0)
			return -1;
		
		
		// calculate how many seconds into the future the given time is
		int secondsIntoFuture = 0;
		for(Entry<String, Integer> time : times.entrySet())
		{
			Integer value = time.getValue();
			switch(time.getKey())
			{
				case "m":
					value *= 60;
					break;
				case "h":
					value *= 3600;
					break;
				case "d":
					value *= 86400;
					break;
				case "w":
					value *= 604800;
					break;
				default:
					return -1;
			}
			
			secondsIntoFuture += value;
		}
	    
		// add that to the current time
	    return (System.currentTimeMillis() / 1000L) + secondsIntoFuture;
	}

}
