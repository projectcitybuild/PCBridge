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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.tasks.GetUuidTask;
import com.pcb.pcbridge.utils.MessageBuilder;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import com.pcb.pcbridge.utils.commands.CommandPrompt;

public class CommandBuyHome extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "buyhome";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Confirms the purchase of a home";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.home.buy";
	}

	@Override
	public String GetUsage()
	{
		return "/buyhome";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(!args.IsPlayer())
		{
			args.GetSender().sendMessage("ERROR: Only players can use this command");
			return true;
		}
		
		if(args.GetArgs().length > 0)
			return false;
		
		// pull data from the prompt (created when clicking a sign)
		CommandPrompt prompt = GetPromptManager().GetPrompt("BUYHOME", args.GetSender());
		if(prompt == null)
			return true;
		
		double price = (double) prompt.GetData("PRICE");
		String owner = (String) prompt.GetData("OWNER");
		Sign sign = (Sign) prompt.GetData("SIGN");

		Economy economy = PCBridge.GetVaultHook().GetEconomy();
		
		// check balance again just in case they somehow lost money in the time
		// between clicking the sign and using this command
		if(!economy.has(args.GetPlayer(), price))
		{
			args.GetSender().sendMessage(ChatColor.RED + "You do not have enough money to buy this house");
			return true;
		}
		
		PCBridge.NewChain()
			.asyncFirst( () -> {
				economy.withdrawPlayer(args.GetPlayer(), price);
				return null;
			})
			.syncLast( test -> {
				String message = new MessageBuilder()
				.Colour(ChatColor.GREEN)
				.String("You have purchased a home for $%s", price)
				.Build();
			
				args.GetSender().sendMessage(message);
				
				sign.setLine(0, "" + ChatColor.BLUE + ChatColor.BOLD + "[OWNER]");
				sign.setLine(1, TruncateAlias(args.GetPlayer().getName()) );
				sign.setLine(2, "");
				sign.setLine(3, "");
				sign.update();
			})
			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), owner))
			.async( uuid -> {
				Player player = GetEnv().GetServer().getPlayer(uuid);
				economy.depositPlayer(player, price);
				
				return player;
			})
			.syncLast(player -> {				
				if(player.isOnline())
				{
					String message = new MessageBuilder()
						.Colour(ChatColor.GREEN)
						.String("Your home has been purchased for $%s by %s", price, args.GetSender().getName())
						.Build();
					
					player.sendMessage(message);
					
					Firework firework = (Firework) args.GetPlayer().getWorld().spawnEntity(args.GetPlayer().getLocation(), EntityType.FIREWORK);
					FireworkEffect effect = FireworkEffect.builder()
							.with(Type.BALL_LARGE)
							.withColor(Color.AQUA)
							.withTrail()
							.build();
					
					FireworkMeta meta = firework.getFireworkMeta();
					meta.setPower(1);
					meta.addEffect(effect);
					firework.setFireworkMeta(meta);
				}
			})
			.execute();
		
		
		return true;
	}
	
	private String TruncateAlias(String alias)
	{
		return alias.substring(0, Math.min(16, alias.length()) );
	}

}
