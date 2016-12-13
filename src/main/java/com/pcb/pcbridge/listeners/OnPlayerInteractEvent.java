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
package com.pcb.pcbridge.listeners;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.pcb.pcbridge.utils.MessageBuilder;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.utils.listeners.AbstractListener;

public final class OnPlayerInteractEvent extends AbstractListener
{
	private String _signSale = "" + ChatColor.DARK_RED + ChatColor.BOLD + "[FOR SALE]";
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnInteract(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Material block = event.getClickedBlock().getType();
			if(block == Material.SIGN || block == Material.SIGN_POST || block == Material.WALL_SIGN)
			{
				InteractSign(event);
			}
		}
	}
	
	/**
	 * Checks if a created sign is in the format for selling a house
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event)
	{
		if(event.getLine(0).equalsIgnoreCase("[FOR SALE]"))
		{
			if(event.getLine(1).matches("[$][0-9]+") && event.getLine(2).length() == 0 && event.getLine(3).length() == 0)
			{
				event.setLine(0, _signSale);
				event.setLine(1, event.getLine(1) );
				event.setLine(3, TruncateAlias(event.getPlayer().getName()) );

				event.getPlayer().sendMessage("Your home is now for sale");
			}
		}
	}
	
	/**
	 * Checks if the clicked sign is for a house for sale
	 * 
	 * @param event
	 */
	private void InteractSign(PlayerInteractEvent event)
	{
		Sign sign = (Sign)event.getClickedBlock().getState();
		
		if(sign.getLine(0).equals(_signSale) && sign.getLine(1).matches("[$][0-9]+") && sign.getLine(3).length() > 0)
		{			
			if(event.getPlayer().getName().equals(sign.getLine(3)))
			{
				event.getPlayer().sendMessage(ChatColor.GRAY + "You can't buy your own house");
				//return;
			}

			double price = Double.parseDouble( sign.getLine(1).replace("$", "") );
			
			Economy economy = PCBridge.GetVaultHook().GetEconomy();
			if(!economy.has(event.getPlayer(), price))
			{
				event.getPlayer().sendMessage(ChatColor.RED + "You do not have enough money to buy this house");
				return;
			}
			
			GetEnv().GetPromptManager().Register("BUYHOME", event.getPlayer(), 15)
				.SetData("PRICE", price)
				.SetData("OWNER", sign.getLine(3))
				.SetData("SIGN", sign);
			
			String message = new MessageBuilder()
				.Colour(ChatColor.AQUA)
				.String("You are about to purchase a home for $%s. Type /buyhome to proceed", price)
				.Build();
			
			event.getPlayer().sendMessage(message);
		}
	}

	private String TruncateAlias(String alias)
	{
		return alias.substring(0, Math.min(16, alias.length()) );
	}
}
