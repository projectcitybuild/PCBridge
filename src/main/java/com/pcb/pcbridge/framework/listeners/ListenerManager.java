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
