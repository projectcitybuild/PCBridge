package com.projectcitybuild.modules.announcements

import com.projectcitybuild.pcbridge.core.utils.Cancellable
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import net.md_5.bungee.api.chat.TextComponent
import java.util.concurrent.TimeUnit

class AnnouncementsModule: PluginModule {
    private val timerId = "scheduled_announcements"
    private var index = 0
    private var timer: Cancellable? = null

    override fun register(module: ModuleDeclaration) {
        module {
            val config = container.config.get()
            val intervalInMins = config.announcements.intervalInMins
            val messages = config.announcements.messages

            timer = container.timer.scheduleRepeating(
                identifier = timerId,
                delay = 0,
                repeatingInterval = 10,
                // repeatingInterval = intervalInMins.toLong(),
                unit = TimeUnit.SECONDS,
                work = {
                    val message = messages[index]
                    container.spigotServer.broadcastMessage(
                        TextComponent(message)
                    )
                    index = index + 1 % messages.size - 1
                }
            )
        }
    }

    override fun unregister() {
        timer?.cancel()
        timer = null
    }
}