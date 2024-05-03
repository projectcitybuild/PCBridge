package com.projectcitybuild.support.spigot

interface SpigotIntegration {
    suspend fun onEnable() = run { }
    suspend fun onDisable() = run { }
}
