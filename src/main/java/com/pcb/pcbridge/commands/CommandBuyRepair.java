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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandBuyRepair extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "buyrepair";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Repairs the current item-in-hand for a fee";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.eco.buyrepair";
	}

	@Override
	public String GetUsage()
	{
		return "/buyrepair";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		/*if(!args.IsPlayer())
		{
			args.GetSender().sendMessage("ERROR: Only players can use this command");
			return true;
		}
	
		if(args.GetArgs().length > 1)
			return false;
		
		if(args.GetArgs().length == 1)
		{
			if(!args.GetArg(0).equalsIgnoreCase("confirm"))
			{
				args.GetSender().sendMessage("ERROR: Invalid command. Did you mean /buyrepair confirm ?");
				return true;
			}
			
			// use prompt data
			
			PlayerInventory inv = args.GetPlayer().getInventory();
			ItemStack item = inv.getItemInMainHand();
			item.setDurability((short) 0);		
			args.GetPlayer().updateInventory();
		}
		
		PlayerInventory inv = args.GetPlayer().getInventory();
		ItemStack item = inv.getItemInMainHand();
		
		if(item != null)
		{
			args.GetSender().sendMessage("ERROR: You are not holding a tool in your main hand");
			return true;
		}
		
		if(item.getType().isBlock())
		{
			args.GetSender().sendMessage("ERROR: That item cannot be repaired");
			return true;
		}
		
		if(item.getDurability() == 0)
		{
			args.GetSender().sendMessage("ERROR: This item does not need repairing");
			return true;
		}
		
		GetPromptManager().Register("BUYREPAIR", args.GetSender(), 15)
			.SetData("ITEM", item);
		
		args.GetSender().sendMessage("You are about to repair SOMETHING for $0.00. Type /buyrepair confirm to proceed.");
		*/
		return true;
	}

}
