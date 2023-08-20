package com.projectcitybuild.support.spigot

import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.isA
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SpigotSchedulerTest {
    private lateinit var plugin: JavaPlugin
    private lateinit var server: Server
    private lateinit var bukkitScheduler: BukkitScheduler
    private lateinit var scheduler: SpigotScheduler

    @BeforeEach
    fun setUp() {
        plugin = mock(JavaPlugin::class.java)
        server = mock(Server::class.java)
        bukkitScheduler = mock(BukkitScheduler::class.java)
        scheduler = SpigotScheduler(plugin)

        whenever(plugin.server).thenReturn(server)
        whenever(server.scheduler).thenReturn(bukkitScheduler)
    }

    private fun processSyncTasks() {
        argumentCaptor<Runnable>().apply {
            verify(bukkitScheduler).scheduleSyncDelayedTask(eq(plugin), capture())
            firstValue.run()
        }
    }

    private fun processAsyncTasks() {
        argumentCaptor<Runnable>().apply {
            verify(bukkitScheduler).runTaskAsynchronously(eq(plugin), capture())
            firstValue.run()
        }
    }

    @Test
    fun `schedules sync task`() {
        var didRun = false
        val task = { didRun = true }
        scheduler.sync(task)

        processSyncTasks()
        assertTrue(didRun)
    }

    @Test
    fun `schedules async task`() {
        whenever(bukkitScheduler.runTaskAsynchronously(eq(plugin), isA(Runnable::class.java)))
            .thenReturn(mock(BukkitTask::class.java))

        var didRun = false
        val task = scheduler.async<Unit> { resolve ->
            didRun = true
            resolve(Unit)
        }
        assertFalse(didRun)

        task.start()
        processAsyncTasks()
        assertTrue(didRun)
    }

    @Test
    fun `cancels async task`() {
        val bukkitTask = mock(BukkitTask::class.java)
        whenever(bukkitScheduler.runTaskAsynchronously(eq(plugin), isA(Runnable::class.java)))
            .thenReturn(bukkitTask)

        var didRun = false
        val task = scheduler.async<Unit> { resolve ->
            didRun = true
            resolve(Unit)
        }
        assertFalse(didRun)

        val cancellable = task.start()
        cancellable.cancel()
        verify(bukkitTask).cancel()
    }

    @Test
    fun `async task resolves to a value`() {
        whenever(bukkitScheduler.runTaskAsynchronously(eq(plugin), isA(Runnable::class.java)))
            .thenReturn(mock(BukkitTask::class.java))

        val task = scheduler.async<Boolean> { resolve ->
            resolve(true)
        }

        var didRun = false
        task.startAndSubscribe { resolvedValue ->
            didRun = resolvedValue
        }
        assertFalse(didRun)

        processAsyncTasks()
        assertTrue(didRun)
    }
}