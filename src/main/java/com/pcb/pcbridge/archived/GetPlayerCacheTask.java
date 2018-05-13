//package com.pcb.pcbridge.archived;
//
//import java.util.UUID;
//
//import com.pcb.pcbridge.archived.PlayerConfig;
//import com.pcb.pcbridge.archived.utils.cache.StaticCache;
//
//public class GetPlayerCacheTask {
//
//	/**
//	 * Fetches a player's config from the cache (or puts it there if not found)
//	 */
//	public static PlayerConfig Get(Environment env, UUID uuid)
//	{
//		StaticCache<UUID, PlayerConfig> cache = env.GetPlayerCache();
//
//		PlayerConfig config = cache.Get(uuid);
//		if(config == null)
//			config = new PlayerConfig(env.GetPlayerFolder(), uuid);
//
//		return config;
//	}
//
//}
