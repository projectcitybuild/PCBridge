package com.projectcitybuild.spigot.environment

import me.lucko.luckperms.api.LuckPermsApi
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPluginHook(private val plugin: WeakReference<JavaPlugin>) {
    var permissions: LuckPermsApi? = null
        private set

    var chat: Chat? = null
        private set

    init {
        setupPermissionHook()
        setupChatHook()
    }

    private fun setupPermissionHook() {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi::class.java)
        if (provider != null) {
            permissions = provider.provider
        }
    }

    private fun setupChatHook() {
        val plugin = this.plugin.get() ?: throw Exception("Plugin reference is null")
        val rsp = plugin.server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp.provider
    }
}
