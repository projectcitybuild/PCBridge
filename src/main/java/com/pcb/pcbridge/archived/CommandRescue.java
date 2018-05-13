//package com.pcb.pcbridge.archived;
//
//import org.spigot.Bukkit;
//import org.spigot.ChatColor;
//import org.spigot.Location;
//import org.spigot.World;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandRescue extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "rescue";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Sets the login coordinates of an offline player to the spawn of their current world";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.util.rescue";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/rescue <name> [x] [y] [z] [world]";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(args.GetArgs().length == 0 || args.GetArgs().length > 5)
//			return false;
//
//		Location location;
//		if(args.GetArgs().length == 1)
//		{
//			// console must specify coordinates
//			if(!args.IsPlayer())
//			{
//				args.GetSender().sendMessage(ChatColor.RED + "Console must specify coordinates to use this command");
//				return true;
//			}
//
//			// if no coordinates given, use the sender's location
//			location = args.GetPlayer().getLocation();
//		}
//		else
//		{
//			double x, y, z;
//			try
//			{
//				x = Double.parseDouble( args.GetArg(1) );
//				y = Double.parseDouble( args.GetArg(2) );
//				z = Double.parseDouble( args.GetArg(3) );
//			}
//			catch(NumberFormatException e)
//			{
//				args.GetSender().sendMessage(ChatColor.RED + "Invalid coordinates");
//				return true;
//			}
//
//			if(args.GetArgs().length == 4)
//			{
//				// if no world given, use the sender's current
//				location = new Location(args.GetPlayer().getWorld(), x, y, z);
//			}
//			else
//			{
//				// if world given, use it
//				World world = Bukkit.getWorld(args.GetArg(4));
//				if(world == null)
//				{
//					args.GetSender().sendMessage(ChatColor.RED + "World " + args.GetArg(4) + " does not exist");
//					return true;
//				}
//
//				location = new Location(world, x, y, z);
//			}
//		}
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FindOrFetch(GetEnv().GetServer(), args.GetArg(0)) )
//			.abortIfNull()
//			.async(uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
//			.syncLast(config -> {
//				config.NeedsRescue = true;
//				config.RescueCoords = location;
//
//				try
//				{
//					config.Save();
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//
//				String message = new MessageBuilder()
//					.Colour(ChatColor.GRAY)
//					.String("%s has been set to next login at: %s", args.GetArg(0), location)
//					.Build();
//
//				args.GetSender().sendMessage(message);
//			})
//			.execute();
//
//
//		return true;
//	}
//
//}
