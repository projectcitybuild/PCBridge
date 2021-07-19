package com.projectcitybuild.platforms.spigot.environment

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit

class SpigotPluginHook {

    private var _permissions: LuckPerms? = null
    var permissions: LuckPerms? = null
        get() {
            if (_permissions == null) {
                _permissions = setupPermissionHook()
            }
            return _permissions
        }

    private fun setupPermissionHook(): LuckPerms? {
        val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if (provider != null) {
            return provider.provider
        }
        println("ERROR: LuckPerms not found - searching...")
        return LuckPermsProvider.get()
    }
}
