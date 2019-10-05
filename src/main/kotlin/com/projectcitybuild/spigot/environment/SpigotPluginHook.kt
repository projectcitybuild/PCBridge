package com.projectcitybuild.spigot.environment

import me.lucko.luckperms.api.LuckPermsApi
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPluginHook(private val plugin: WeakReference<JavaPlugin>) {
    var permissions: LuckPermsApi? = null
        private set

    init {
        setupPermissionHook()
    }

    private fun setupPermissionHook() {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi::class.java)
        if (provider != null) {
            permissions = provider.provider
        }
    }
}
