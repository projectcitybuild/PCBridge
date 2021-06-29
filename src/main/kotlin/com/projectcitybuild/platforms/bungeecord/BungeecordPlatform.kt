package com.projectcitybuild.platforms.bungeecord

import com.google.common.io.ByteStreams
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordEnvironment
import com.projectcitybuild.platforms.bungeecord.listeners.BanConnectionListener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BungeecordPlatform: Plugin() {

    private val environment: EnvironmentProvider by lazy {
        BungeecordEnvironment(plugin = this, logger = logger)
    }
    private var listenerDelegate: BungeecordListenerDelegate? = null

    private var _networkClients: NetworkClients? = null
    private val networkClients: NetworkClients
        get() {
            if (_networkClients == null) {
                _networkClients = createAPIProvider()
            }
            return _networkClients!!
        }

    override fun onEnable() {
        createDefaultConfig()

        val listenerDelegate = BungeecordListenerDelegate(plugin = this, environment = environment)
        registerListeners(listenerDelegate)
        this.listenerDelegate = listenerDelegate
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        listenerDelegate = null
    }

    private fun registerListeners(delegate: BungeecordListenerDelegate) {
        arrayOf(
                BanConnectionListener(environment, networkClients)
        )
        .forEach { listener -> delegate.register(listener) }
    }

    private fun createDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val file = File(dataFolder, "config.yml")

        if (!file.exists()) {
            // FIXME
//            try {
//                getResourceAsStream("config.yml").use { `in` ->
//                    val out = FileOutputStream(file)
//                    ByteStreams.copy(in, out)
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
        }

        if (!file.exists()) {
            val configProvider = ConfigurationProvider
                    .getProvider(YamlConfiguration::class.java)

            val config = configProvider.load(file)

            config.set(PluginConfig.Settings.MAINTENANCE_MODE().key, false)
            config.set(PluginConfig.API.KEY().key, "")
            config.set(PluginConfig.API.BASE_URL().key, "")
            config.set(PluginConfig.API.IS_LOGGING_ENABLED().key, false)

            configProvider.save(config, file)
        }
    }

    private fun createAPIProvider(): NetworkClients {
        val isLoggingEnabled = environment.get(PluginConfig.API.IS_LOGGING_ENABLED()) as? Boolean
                ?: throw Exception("Could not cast is_logging_enabled to Boolean")

        val pcbClient = PCBClient(
                authToken = environment.get(PluginConfig.API.KEY()) as? String
                        ?: throw Exception("Could not cast auth token to String"),
                baseUrl = environment.get(PluginConfig.API.BASE_URL()) as? String
                        ?: throw Exception("Could not cast base url to String"),
                withLogging = isLoggingEnabled
        )
        val mojangClient = MojangClient(
                withLogging = isLoggingEnabled
        )
        return NetworkClients(pcb = pcbClient, mojang = mojangClient)
    }
}