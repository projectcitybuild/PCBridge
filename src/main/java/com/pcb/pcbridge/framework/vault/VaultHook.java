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
package com.pcb.pcbridge.framework.vault;

import com.google.inject.Singleton;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import java.util.Optional;

@Singleton
public class VaultHook {

	private Optional<Permission> permission;
	private Optional<Chat> chat;

	/**
	 * Gets a hook to a permission plugin
	 *
	 * @return Permission plugin
	 */
	public Optional<Permission> getPermission() {
		return permission;
	}

	/**
	 * Gets a hook to a chat plugin
	 *
	 * @return Chat plugin
	 */
	public Optional<Chat> getChat() {
		return chat;
	}


	/**
	 * Attempts to hook into any Permission plugin
	 * available on the server
	 *
	 * @param plugin
	 * @throws PermissionHookFailedException
	 */
	public void hookIntoPermissionPlugin(Plugin plugin) throws PermissionHookFailedException {
		final RegisteredServiceProvider<Permission> provider = plugin
				.getServer()
				.getServicesManager()
				.getRegistration(Permission.class);

		if (provider == null) {
			throw new PermissionHookFailedException("Failed to get permission provider. Is there a permission plugin installed?");
		}

		permission = Optional.ofNullable(provider.getProvider());
		if(!permission.isPresent()) {
			throw new PermissionHookFailedException("Failed to hook into a permissions plugin");
		}

		plugin.getLogger().info("Hooked into a permission plugin");
	}

	/**
	 * Attempts to hook into any Chat plugin
	 * available on the server
	 *
	 * @param plugin
	 * @throws ChatHookFailedException
	 */
	public void hookIntoChatPlugin(Plugin plugin) throws ChatHookFailedException {
		final RegisteredServiceProvider<Chat> provider = plugin
				.getServer()
				.getServicesManager()
				.getRegistration(Chat.class);

		if (provider == null) {
			throw new ChatHookFailedException("Failed to get chat provider. Is there a chat plugin installed?");
		}

		chat = Optional.ofNullable(provider.getProvider());
		if (!chat.isPresent()) {
			throw new ChatHookFailedException("Failed to hook into a chat plugin?");
		}

        plugin.getLogger().info("Hooked into a chat plugin");
	}
	
	/**
	 * Removes the hook to the permission plugin
	 */
	public void unhookFromPermissionPlugin() {
		permission = Optional.empty();
	}
	
	/**
	 * Removes the hook to the chat plugin
	 */
	public void unhookFromChatPlugin() {
		chat = Optional.empty();
	}
	
}
