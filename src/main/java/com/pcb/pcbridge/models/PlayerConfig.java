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
package com.pcb.pcbridge.models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Represents persistent data inside a player's file
 */
public class PlayerConfig {

	public boolean IsMuted;
	public boolean IsSwearblockEnabled;
	public String Prefix;
	public String Suffix;
	public ItemStack[] Chest;
	public boolean NeedsRescue;
	public Location RescueCoords;
	public Location LastPos;
	
	
	private final File _file;
	
	public PlayerConfig(File playerFolder, UUID uuid)
	{
		this._file = new File(playerFolder, uuid + ".yml");
		
		if(!_file.exists())
		{
			this.IsMuted = false;
			this.IsSwearblockEnabled = false;
			this.Prefix = null;
			this.Suffix = null;
			this.Chest = null;
			this.NeedsRescue = false;
			this.RescueCoords = null;
			this.LastPos = null;
			
			try
			{
				Save();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			YamlConfiguration reader = GetReader();
			this.IsMuted = reader.getBoolean("chat.muted");
			this.IsSwearblockEnabled = reader.getBoolean("chat.swearblock");
			this.Prefix = reader.getString("chat.prefix");
			this.Suffix = reader.getString("chat.suffix");
			this.NeedsRescue = reader.getBoolean("server.rescue.needed");
			this.RescueCoords = (Location) reader.get("server.rescue.coordinates");
			this.LastPos = (Location) reader.get("server.lastpos");
			
			@SuppressWarnings("unchecked")
			ArrayList<ItemStack> items = (ArrayList<ItemStack>) reader.get("server.chest");
			if(items != null)
			{
				this.Chest = (ItemStack[]) items.toArray(new ItemStack[items.size()]);
			}			
		}
	}
	
	public File GetFile()
	{
		return _file;
	}
	
	public YamlConfiguration GetReader()
	{
		return YamlConfiguration.loadConfiguration(_file);
	}
	
	public void Save() throws IOException
	{
		YamlConfiguration reader = GetReader();
		reader.set("chat.muted", IsMuted);
		reader.set("chat.swearblock", IsSwearblockEnabled);
		reader.set("chat.prefix", Prefix);
		reader.set("chat.suffix", Suffix);
		reader.set("server.chest", Chest);
		reader.set("server.rescue.needed", NeedsRescue);
		reader.set("server.rescue.coordinates", RescueCoords);
		reader.set("server.lastpos", LastPos);
		reader.save(GetFile());
	}
	
	@Override
	public String toString()
	{
		return String.format("isMuted: %s, Chest: %s", IsMuted, Chest);
	}
	
}
