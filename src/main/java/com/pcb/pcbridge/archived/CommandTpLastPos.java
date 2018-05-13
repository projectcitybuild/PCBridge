//package com.pcb.pcbridge.archived;
//
//import org.spigot.ChatColor;
//import org.spigot.entity.Player;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.framework.commands.AbstractCommand;
//
//public class CommandTpLastPos extends AbstractCommand {
//
//	@Override
//	public String GetName()
//	{
//		return "tplastpos";
//	}
//
//	@Override
//	public String GetDescription()
//	{
//		return "Teleports to the last known position of an offline player";
//	}
//
//	@Override
//	public String GetPermission()
//	{
//		return "pcbridge.util.tplastpos";
//	}
//
//	@Override
//	public String GetUsage()
//	{
//		return "/tplastpos <name>";
//	}
//
//	@Override
//	public boolean OnExecute(CommandArgs args)
//	{
//		if(!args.IsPlayer())
//		{
//			args.GetSender().sendMessage(ChatColor.RED + "Only players in-game can use this command.");
//			return true;
//		}
//
//		if(args.GetArgs().length != 1)
//			return false;
//
//		// if confirming a tp, use the prompt data instead
//		if(args.GetArg(0).equalsIgnoreCase("confirm"))
//		{
//			CommandPrompt prompt = GetPromptManager().GetPrompt("TPLASTPOS", args.GetSender());
//			if(prompt != null)
//			{
//				prompt.Run();
//				GetPromptManager().DestroyPrompt("TPLASTPOS", args.GetSender());
//				return true;
//			}
//		}
//
//		// check that the player isn't online
//		String alias = args.GetArg(0);
//		for(Player player : GetEnv().GetServer().getOnlinePlayers())
//		{
//			if(player.getName().equalsIgnoreCase(alias))
//			{
//				args.GetSender().sendMessage(ChatColor.RED + "Cannot teleport to an online player.");
//				return true;
//			}
//		}
//
//		PCBridge.NewChain()
//			.asyncFirst( () -> GetUuidTask.FetchUUID(alias))
//			.async( uuid -> GetPlayerCacheTask.Get(GetEnv(), uuid))
//			.syncLast( config -> {
//				if(config.LastPos == null)
//				{
//					args.GetSender().sendMessage(ChatColor.RED + "No last known position for " + alias);
//					return;
//				}
//
//				args.GetSender().sendMessage(ChatColor.GRAY + "Last known position: " + config.LastPos);
//
//				String promptMessage = new MessageBuilder()
//					.Colour(ChatColor.AQUA)
//					.String("Type ")
//					.Colour(ChatColor.WHITE)
//					.String("/tplastpos confirm")
//					.Reset().Colour(ChatColor.AQUA)
//					.String(" to teleport there")
//					.Build();
//
//				args.GetSender().sendMessage(promptMessage);
//
//				GetPromptManager().Register("TPLASTPOS", args.GetSender(), () -> {
//					args.GetSender().sendMessage(ChatColor.GRAY + "Teleporting...");
//					args.GetPlayer().teleport(config.LastPos);
//				});
//			})
//			.execute();
//
//		return true;
//	}
//
//}
