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

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class VaultHook {

	private Permission _permission;
	private Chat _chat;
	
	public Permission GetPermission()
	{
		return _permission;
	}
	
	public Chat GetChat()
	{
		return _chat;
	}
	
	public VaultHook(Plugin plugin)
	{
		HookPermission(plugin);
		HookChat(plugin);
	}

	/**
	 * Uses Vault to hook into whatever permission plugin is present
	 * 
	 * @param plugin
	 * @return boolean	Hook success
	 */
	public boolean HookPermission(Plugin plugin)
	{
		RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) 
            _permission = permissionProvider.getProvider();
        
        if(_permission == null)
        	plugin.getLogger().severe("Failed to hook into Permissions plugin");
        else
        	plugin.getLogger().info("Hooked into Permission plugin");
        
        return _permission != null;
	}
	
	/**
	 * Uses Vault to hook into whatever chat plugin is present
	 * 
	 * @param plugin
	 * @return boolean	Hook success
	 */
	public boolean HookChat(Plugin plugin)
	{
		RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(Chat.class);
		if (chatProvider != null) 
            _chat = chatProvider.getProvider();
        
        if(_permission == null)
        	plugin.getLogger().severe("Failed to hook into Permissions plugin");
        else
        	plugin.getLogger().info("Hooked into Permission plugin");
        
        return _chat != null;
	}
	
	/**
	 * Removes the hook to the permission plugin
	 */
	public void UnhookPermission()
	{
		_permission = null;
	}
	
	/**
	 * Removes the hook to the chat plugin
	 */
	public void UnhookChat()
	{
		_chat = null;
	}
	
}
