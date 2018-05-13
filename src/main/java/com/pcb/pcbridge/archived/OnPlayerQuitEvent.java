//package com.pcb.pcbridge.archived;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import org.spigot.entity.Player;
//import org.spigot.event.EventHandler;
//import org.spigot.event.EventPriority;
//import org.spigot.event.player.PlayerQuitEvent;
//
//import com.pcb.pcbridge.archived.utils.cache.StaticCache;
//
//public final class OnPlayerQuitEvent extends AbstractListener
//{
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void OnPlayerLogin(PlayerQuitEvent event)
//	{
//		RemovePlayerCache(event.getPlayer());
//	}
//
//	@Override
//	public void OnDisable()
//	{
//		// manually save every player's config when this listener gets disabled
//		this._environment.GetServer().getOnlinePlayers().forEach(player -> {
//			RemovePlayerCache(player);
//		});
//	}
//
//	/**
//	 * Saves a player's last known position and then removes
//	 * their PlayerConfig from the cache
//	 */
//	public void RemovePlayerCache(Player player)
//	{
//		StaticCache<UUID, PlayerConfig> cache = GetEnv().GetPlayerCache();
//		if(cache != null)
//		{
//			PlayerConfig config = cache.Get(player.getUniqueId());
//			config.LastPos = player.getLocation();
//			try {
//				config.Save();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			cache.Forget(player.getUniqueId());
//		}
//	}
//}