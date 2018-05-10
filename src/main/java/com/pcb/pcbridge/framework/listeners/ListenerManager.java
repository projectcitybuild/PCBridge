package com.pcb.pcbridge.framework.listeners;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.HandlerList;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

	private final WeakReference<Plugin> plugin;
	private final List<EventListener> listeners = new ArrayList<>();
	
	public ListenerManager(Plugin plugin) {
		this.plugin = new WeakReference<>(plugin);
	}

	/**
	 * Registers a collection of event listeners
	 *
	 * @param listeners	Listeners to register
	 */
	public void registerListeners(EventListener[] listeners) {
		final Plugin plugin = this.plugin.get();
		if(plugin == null) {
			return;
		}

		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		for(EventListener listener : listeners) {
			pluginManager.registerEvents((Listener)listener, plugin);
			listener.onRegister();

			this.listeners.add(listener);
		}
	}

	/**
	 * Unregisters and deallocates all event
	 * listeners
	 */
	public void unregisterAll() {
		for(EventListener listener : listeners) {
			listener.onDeregister();
		}
		HandlerList.unregisterAll();
		listeners.clear();
	}

}
