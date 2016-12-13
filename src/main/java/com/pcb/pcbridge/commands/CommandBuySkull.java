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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;

public class CommandBuySkull extends AbstractCommand {

	private final int SKULL_COST = 50;
	private final Pattern pattern = Pattern.compile("^(player:).*?$");
	
	@Override
	public String GetName() 
	{
		return "buyskull";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Buys a skull of the given player for $50 each";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.eco.buyskull";
	}

	@Override
	public String GetUsage()
	{
		return "/buyskull player:<name> [quantity]";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{	
		if(!args.IsPlayer())
		{
			args.GetSender().sendMessage(ChatColor.RED + "Only players in-game can use this command.");
			return true;
		}
		
		if(args.GetArgs().length == 0 || args.GetArgs().length > 2)
			return false;
		
		if(args.GetArgs().length == 2 && !StringUtils.isNumeric(args.GetArg(1)))
			return false;
				
		// check input for player name
		String alias = args.GetArg(0);

	    Matcher m = pattern.matcher(alias);
	    if(!m.find())
	    	return false;
	    
	    // strip 'player:' from name
	    String name = alias.replaceFirst("^(player:)", "");
	    
	    int quantity = args.GetArgs().length == 2 ? Integer.parseInt(args.GetArg(1)) : 1;
	    int cost = SKULL_COST * quantity;
				
		// ensure player has enough money for the purchase
		Economy economy = PCBridge.GetVaultHook().GetEconomy();
		if(!economy.has(args.GetPlayer(), cost))
		{
			args.GetSender().sendMessage(ChatColor.RED + "You do not have enough money (price: $" + cost + ")");
			return true;
		}
		
		// ensure player's inventory isn't full
		PlayerInventory inventory = args.GetPlayer().getInventory();
		if(inventory.firstEmpty() == -1)
		{
			args.GetSender().sendMessage(ChatColor.RED + "Your inventory is full. Please clear a slot and try again.");
			return true;
		}
	    
	    // create skull and give it to the player
	    ItemStack skull = new ItemStack(Material.SKULL_ITEM, quantity, (short) 3);
	    SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(name);
		skull.setItemMeta(meta);
		inventory.addItem(skull);
		
		economy.withdrawPlayer(args.GetPlayer(), cost);
		
		args.GetSender().sendMessage(ChatColor.GREEN + "Purchased skull of " + name + " for $" + cost);
	    
		return true;
	}

}
