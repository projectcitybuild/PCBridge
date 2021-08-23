package com.projectcitybuild.platforms.spigot

import com.google.common.io.ByteStreams
import com.projectcitybuild.core.entities.Channel
import com.projectcitybuild.core.entities.SubChannel
import com.projectcitybuild.platforms.spigot.environment.SpigotLogger
import com.projectcitybuild.platforms.spigot.listeners.ChatListener
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener

class SpigotPlatform: JavaPlugin(), PluginMessageListener {

    private var commandDelegate: SpigotCommandDelegate? = null
    private var listenerDelegate: SpigotListenerDelegate? = null

    private val spigotLogger = SpigotLogger(logger)

    override fun onEnable() {
//        val commandDelegate = SpigotCommandDelegate(plugin = weakRef, environment = environment)
//        registerCommands(delegate = commandDelegate)
//        this.commandDelegate = commandDelegate
//
        val listenerDelegate = SpigotListenerDelegate(plugin = this, spigotLogger)
        registerListeners(delegate = listenerDelegate)
        this.listenerDelegate = listenerDelegate

        server.messenger.registerOutgoingPluginChannel(this, Channel.BUNGEECORD)
        server.messenger.registerIncomingPluginChannel(this, Channel.BUNGEECORD, this)
    }

    override fun onDisable() {
        listenerDelegate?.unregisterAll()

        commandDelegate = null
        listenerDelegate = null

        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)
    }

    private fun registerCommands(delegate: SpigotCommandDelegate) {
    }

    private fun registerListeners(delegate: SpigotListenerDelegate) {
        arrayOf(
            ChatListener()
        )
        .forEach { listenerDelegate?.register(it) }
    }

    override fun onPluginMessageReceived(channel: String?, player: Player?, message: ByteArray?) {
        if (channel != Channel.BUNGEECORD) return

        val input = ByteStreams.newDataInput(message)
        val subchannel = input.readUTF()

        if (subchannel == SubChannel.GLOBAL_CHAT) {

        }
    }
}
