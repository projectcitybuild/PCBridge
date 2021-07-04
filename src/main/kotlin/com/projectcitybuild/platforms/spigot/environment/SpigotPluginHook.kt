package com.projectcitybuild.platforms.spigot.environment
//
//import net.luckperms.api.LuckPerms
//import org.bukkit.Bukkit
//
//class SpigotPluginHook {
//    var permissions: LuckPerms? = null
//        private set
//
//    init {
//        setupPermissionHook()
//    }
//
//    private fun setupPermissionHook() {
//        val provider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
//        if (provider != null) {
//            permissions = provider.provider
//        }
//    }
//}
