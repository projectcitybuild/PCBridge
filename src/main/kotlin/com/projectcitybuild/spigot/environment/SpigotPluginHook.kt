package com.projectcitybuild.spigot.environment

import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotPluginHook(private val plugin: WeakReference<JavaPlugin>) {
    var permissions: LuckPerms? = null
        private set

    init {
        setupPermissionHook()
    }

    private fun setupPermissionHook() {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if (provider != null) {
            permissions = provider.provider
        }
    }
}
