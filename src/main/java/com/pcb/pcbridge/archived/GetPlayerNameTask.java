//package com.pcb.pcbridge.archived;
//
//import java.util.UUID;
//
//import org.spigot.entity.Player;
//
//import net.milkbowl.vault.chat.Chat;
//
//import com.pcb.pcbridge.PCBridge;
//
//
//public class GetPlayerNameTask {
//
//	public static String GetFormattedName(Environment env, Player player)
//	{
//		StaticCache<UUID, PlayerConfig> cache = env.GetPlayerCache();
//		PlayerConfig config = cache.Get(player.getUniqueId());
//
//		return GetFormattedName(env, player, config);
//	}
//
//	public static String GetFormattedName(Environment env, Player player, PlayerConfig config)
//	{
//		// build the player's group prefix
//	    String[] groups = PCBridge.GetVaultHook().GetPermission().getPlayerGroups(player);
//        StringBuilder prefixBuilder = new StringBuilder();
//        StringBuilder suffixBuilder = new StringBuilder();
//
//        // add any manual pre/suffixes to their prefix list
//        if(config.Prefix != null)
//        	prefixBuilder.insert(0, config.Prefix);
//
//        for(String group : groups)
//        {
//        	Chat chat = PCBridge.GetVaultHook().GetChat();
//
//        	String prefix = chat.getGroupPrefix(player.getWorld(), group).trim();
//        	String suffix = chat.getGroupSuffix(player.getWorld(), group).trim();
//
//        	// donators have the [$] appear before any other group prefix or the manual prefix
//        	if(group.equalsIgnoreCase("donator"))
//        		prefixBuilder.insert(0, prefix);
//        	else
//        		prefixBuilder.append(prefix);
//
//        	suffixBuilder.append(suffix);
//        }
//
//        if(config.Suffix != null)
//        	suffixBuilder.append(config.Suffix);
//
//        String prefix = prefixBuilder.toString().replace("&", "§");
//        String suffix = suffixBuilder.toString().replace("&", "§");
//
//        String name = prefix + " " + player.getDisplayName() + suffix;
//
//        return name;
//	}
//
//}
