package com.pcb.pcbridge.bukkit.economy.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;

public final class CommandBuyHead extends AbstractCommand 
{		
	public boolean Execute(CommandArgs e) 
	{
		if(!e.IsPlayer)
		{
			MessageHelper.Send(MessageType.ERROR, e.Sender, "Only players in-game can use this command.");
			return true;
		}
		
		if(e.Args.length < 1 || e.Args.length > 2)
			return false;
		
		if(e.Args.length == 2 && !StringUtils.isNumeric(e.Args[1]))
			return false;
				
		// check input for player name
		String input = e.Args[0];
	    String pattern = "^(player:).*?$";
	    Pattern r = Pattern.compile(pattern);

	    Matcher m = r.matcher(input);
	    if(!m.find())
	    	return false;
	    
	    // strip 'player:' from name
	    String name = input.replaceFirst("^(player:)", "");
	    
	    int quantity = (e.Args.length == 2) ? Integer.parseInt(e.Args[1]) : 1;
	    int cost = 50 * quantity;
	    
	    // generate head
	    ItemStack skull = new ItemStack(Material.SKULL_ITEM, quantity, (short) 3);
	    SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(name);
		skull.setItemMeta(meta);
				
		// ensure player has enough money for the purchase
		OfflinePlayer offPlayer = (OfflinePlayer)e.Sender;
		Economy economy = _plugin.GetEconomyHook();
		if(!economy.has(offPlayer, cost))
		{
			MessageHelper.Send(MessageType.ERROR, e.Sender, "You do not have enough money (price: " + cost + ")");
			return true;
		}
		
		// ensure player's inventory isn't full
		Player player = (Player)e.Sender;
		PlayerInventory inventory = player.getInventory();
		if(inventory.firstEmpty() == -1)
		{
			MessageHelper.Send(MessageType.ERROR, e.Sender, "Your inventory is full. Please clear a slot and try again.");
			return true;
		}
		
		economy.withdrawPlayer(offPlayer, cost);
		inventory.addItem(skull);
		    
		MessageHelper.Send(MessageType.SUCCESS, e.Sender, "Purchased player head (" + name + ") for " + cost);
	    
	    
		return true;
	}
}
