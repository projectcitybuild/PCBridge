package com.projectcitybuild.spigot.environment

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPluginHook(private val plugin: WeakReference<JavaPlugin>) {
    var permissions: Permission? = null
        private set

    var chat: Chat? = null
        private set

    init {
        setupPermissionHook()
        setupChatHook()
    }

    private fun setupPermissionHook() {
        val plugin = this.plugin.get() ?: throw Exception("Plugin reference is null")
        val rsp = plugin.server.servicesManager.getRegistration(Permission::class.java)
        permissions = rsp.provider
    }

    private fun setupChatHook() {
        val plugin = this.plugin.get() ?: throw Exception("Plugin reference is null")
        val rsp = plugin.server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp.provider
    }
}
