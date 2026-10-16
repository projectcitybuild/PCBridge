package com.projectcitybuild.pcbridge.platform.paper.listeners

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SpigotListenerRegistryTest {
    @Test
    fun `register should register each listener with plugin manager`() {
        val plugin = mock<JavaPlugin>()
        val server = mock<Server>()
        val pluginManager = mock<PluginManager>()
        whenever(plugin.server).thenReturn(server)
        whenever(server.pluginManager).thenReturn(pluginManager)

        val registry = SpigotListenerRegistry(plugin)
        val first = TestListener()
        val second = TestListener()
        val mccoroutine = mockStatic(
            Class.forName("com.github.shynixn.mccoroutine.bukkit.MCCoroutineKt"),
        )

        mccoroutine.use { mccoroutine ->
            registry.register(first, second)

            mccoroutine.verify(
                { pluginManager.registerSuspendingEvents(first, plugin) },
                org.mockito.Mockito.times(1),
            )
            mccoroutine.verify(
                { pluginManager.registerSuspendingEvents(second, plugin) },
                org.mockito.Mockito.times(1),
            )
        }
    }

    @Test
    fun `unregister all should unregister listeners associated with plugin`() {
        val plugin = mock<JavaPlugin>()
        val registry = SpigotListenerRegistry(plugin)
        val handlerLists = mockStatic(HandlerList::class.java)

        handlerLists.use { handlerLists ->
            registry.unregisterAll()

            handlerLists.verify { HandlerList.unregisterAll(plugin) }
        }
    }

    private class TestListener : Listener {
        @EventHandler
        fun onTestEvent(event: TestEvent) {}
    }

    private class TestEvent : Event() {
        override fun getHandlers(): HandlerList = HANDLERS

        companion object {
            @JvmStatic
            private val HANDLERS = HandlerList()
        }
    }
}
