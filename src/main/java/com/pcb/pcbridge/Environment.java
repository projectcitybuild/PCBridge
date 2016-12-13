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
package com.pcb.pcbridge;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.utils.cache.StaticCache;
import com.pcb.pcbridge.utils.commands.CommandPromptManager;

public class Environment 
{
	private final String DATE_FORMAT_LONG = "EEE, MMM d, yyyy 'at' h:mm a";
	
	private final JavaPlugin _plugin;
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat(DATE_FORMAT_LONG);
	private CommandPromptManager _promptManager;
	
	public Environment(JavaPlugin plugin)
	{
		this._plugin = plugin;
	}
	
	
	public JavaPlugin GetPlugin()
	{
		return _plugin;
		//return (JavaPlugin) Bukkit.getPluginManager().getPlugin(_pluginName);
	}
	
	public Logger GetLogger()
	{
		return _plugin.getLogger();
	}
	
	public Server GetServer()
	{
		return _plugin.getServer();
	}
	
	public FileConfiguration GetConfig()
	{
		return _plugin.getConfig();
	}
	
	public CommandPromptManager GetPromptManager()
	{
		return _promptManager;
	}
	
	public void SetPromptManager(CommandPromptManager cpm)
	{
		this._promptManager = cpm;
	}
	
	public SimpleDateFormat GetDateFormatter()
	{
		return _dateFormat;
	}
	
	/**
	 * Returns the cache containing a PlayerConfig for each online player
	 * 
	 * @return
	 */
	public StaticCache<UUID, PlayerConfig> GetPlayerCache()
	{
		return ((PCBridge)_plugin).GetPlayerCache();
	}
	
	/**
	 * Returns the cache of the given UUID (UUID must be online)
	 * 
	 * @param uuid
	 * @return
	 */
	public PlayerConfig GetPlayerCache(UUID uuid)
	{
		return GetPlayerCache().Get(uuid);
	}
	
	/**
	 * Gets a File instance of the plugin folder
	 * 
	 * @return
	 */
	public File GetDataFolder()
	{
		return _plugin.getDataFolder();
	}
	
	/**
	 * Gets a file instanec of the folder where local player data is stored
	 * 
	 * @return
	 */
	public File GetPlayerFolder()
	{
		String pluginFolder = _plugin.getDataFolder().getAbsolutePath();
		String path = pluginFolder + File.separator + "players";
		return new File(path);
	}
	
}
