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
package com.pcb.pcbridge.utils.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.event.HandlerList;

import com.pcb.pcbridge.Environment;

/**
 * Handles registration and un/subscription of any event listeners
 */
public class ListenerManager {

	private Environment _environment;
	private List<AbstractListener> _listeners = new ArrayList<>();
	
	public ListenerManager(Environment environment, AbstractListener[] listeners)
	{
		this._environment = environment;
		
		for(AbstractListener listener : listeners)
		{
			listener.SetEnv(environment);
			RegisterListener(listener);
		}
	}
	
	/**
	 * Registers the given listener
	 * 
	 * @param listener
	 */
	private void RegisterListener(AbstractListener listener)
	{
		_environment.GetPlugin()
			.getServer()
			.getPluginManager()
			.registerEvents(listener, _environment.GetPlugin());
		
		_listeners.add(listener);
	}
	
	/**
	 * Unregisters all listeners registered by this plugin
	 */
	public void UnregisterAll()
	{
		// unregister every listener from the plugin
		HandlerList.unregisterAll(_environment.GetPlugin());
				
		// call the OnDisable on each listener and then remove it from memory
		Iterator<AbstractListener> iterator = _listeners.iterator();
		while(iterator.hasNext())
		{
			AbstractListener listener = iterator.next();
			listener.OnDisable();
			iterator.remove();
		}
		
		_listeners.clear();
	}
	
}
