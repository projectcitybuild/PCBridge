package com.projectcitybuild.platforms.bungeecord.environment

//import com.projectcitybuild.core.contracts.EnvironmentProvider
//import com.projectcitybuild.core.entities.LogLevel
//import com.projectcitybuild.core.entities.Player
//import com.projectcitybuild.core.entities.PluginConfigPair
//import com.projectcitybuild.core.utilities.AsyncTask
//import com.projectcitybuild.core.utilities.Cancellable
//import net.luckperms.api.LuckPerms
//import net.md_5.bungee.api.plugin.Plugin
//import net.md_5.bungee.config.ConfigurationProvider
//import net.md_5.bungee.config.YamlConfiguration
//import java.io.File
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//class BungeecordEnvironment(
//        private val plugin: Plugin
//) : EnvironmentProvider {
//
//    override fun log(level: LogLevel, message: String) {
//        TODO()
//    }
//
//    override fun <T> get(key: PluginConfigPair<T>): T {
//        // FIXME: stop IO thrashing
//        val file = File(plugin.dataFolder, "config.yml")
//
//        val config = ConfigurationProvider
//                .getProvider(YamlConfiguration::class.java)
//                .load(file)
//
//        return config.get(key.key) as T // FIXME
//    }
//
//    override fun <T> set(key: PluginConfigPair<T>, value: T) {
//        // FIXME: stop IO thrashing
//        val file = File(plugin.dataFolder, "config.yml")
//
//        val config = ConfigurationProvider
//                .getProvider(YamlConfiguration::class.java)
//                .load(file)
//
//        config.set(key.key, value)
//
//        ConfigurationProvider
//                .getProvider(YamlConfiguration::class.java)
//                .save(config, file)
//    }
//
//    override fun get(player: UUID): Player? {
//        TODO()
////        return playerStore.get(player)
//    }
//
//    override fun set(player: Player) {
//        TODO()
////        playerStore.put(player.uuid, player)
//    }
//
//    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
//        TODO()
//    }
//
//    override fun sync(task: () -> Unit) {
//        TODO()
//    }
//
//    override val permissions: LuckPerms? by lazy {
//        TODO("Not implemented yet")
//    }
//}