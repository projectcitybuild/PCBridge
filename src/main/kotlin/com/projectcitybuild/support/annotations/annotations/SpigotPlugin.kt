package com.projectcitybuild.support.annotations.annotations

/**
 *  Represents the required elements needed to register a Bukkit plugin.
 *  This <i>must</i> be placed in the main class of your plugin
 *  (i.e. the class that extends {@link org.bukkit.plugin.java.JavaPlugin}
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
annotation class SpigotPlugin(
    val name: String,
    val version: String,
)
