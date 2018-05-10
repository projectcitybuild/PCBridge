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
