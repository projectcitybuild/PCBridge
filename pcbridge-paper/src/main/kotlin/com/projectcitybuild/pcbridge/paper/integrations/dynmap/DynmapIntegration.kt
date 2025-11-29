package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.config.domain.data.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.data.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.warps.domain.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.paper.features.warps.domain.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class DynmapIntegration(
    private val plugin: JavaPlugin,
    private val spawnRepository: SpawnRepository,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
) : Listener {
    private var adapter: DynmapAdapter? = null
    private val tracer = TracerFactory.make("integration.dynmap")

    suspend fun enable() {
        // Check if DynmapCoreAPI class is present, as this is only available
        // if the dynmap plugin is loaded
        try {
            Class.forName("org.dynmap.DynmapCommonAPI")
        } catch (_: ClassNotFoundException) {
            log.warn { "DynmapCommonAPI not found, most likely due to dynmap not being loaded. Disabling dynmap integration..." }
            return
        }

        log.info { "Registering Dynmap integration..." }
        adapter = DynmapAdapter()
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)

        log.info { "Dynmap integration loaded" }

        updateWarpMarkers()
        updateSpawnMarkers()
    }

    fun disable() {
        if (adapter == null) return

        WarpCreateEvent.getHandlerList().unregister(this)
        WarpDeleteEvent.getHandlerList().unregister(this)
        adapter = null

        logSync.info { "Dynmap integration disabled" }
    }

    @EventHandler
    suspend fun onWarpCreate(
        event: WarpCreateEvent,
    ) = event.scoped(tracer, this::class.java) { updateWarpMarkers() }

    @EventHandler
    suspend fun onWarpDelete(
        event: WarpDeleteEvent,
    ) = event.scoped(tracer, this::class.java) { updateWarpMarkers() }

    @EventHandler
    suspend fun onSpawnUpdate(
        event: SpawnUpdatedEvent,
    ) = event.scoped(tracer, this::class.java) { updateSpawnMarkers() }

    @EventHandler
    suspend fun onRemoteConfigUpdated(
        event: RemoteConfigUpdatedEvent,
    ) = event.scoped(tracer, this::class.java) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.integrations?.dynmapWarpIconName != next.integrations.dynmapWarpIconName) {
            updateWarpMarkers()
        }
        if (prev?.integrations?.dynmapSpawnIconName != next.integrations.dynmapSpawnIconName) {
            updateSpawnMarkers()
        }
    }

    private suspend fun updateWarpMarkers() = tracer.trace("updateWarpMarkers") {
        val adapter = adapter
        if (adapter == null) {
            log.error { "Dynmap integration disabled but attempted to draw warp markers" }
            return@trace
        }
        val config = remoteConfig.latest.config

        try {
            warpRepository.all().forEach { warp ->
                adapter.createMarker(
                    id = "warp_${warp.id}",
                    label = warp.name,
                    world = warp.world,
                    x = warp.x,
                    y = warp.y,
                    z = warp.z,
                    iconName = config.integrations.dynmapWarpIconName,
                    fallbackIconName = "building",
                    setId = "pcbridge_warps",
                    setLabel = "Warps"
                )
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to draw warp markers" }
        }
    }

    private suspend fun updateSpawnMarkers() = tracer.trace("updateSpawnMarkers") {
        val adapter = adapter
        if (adapter == null) {
            log.error { "Dynmap integration disabled but attempted to draw spawn markers" }
            return@trace
        }
        val config = remoteConfig.latest.config

        try {
            spawnRepository.allLoaded().forEach { location ->
                adapter.createMarker(
                    id = "spawn_${location.world.uid}",
                    label = "Spawn",
                    world = location.world.name,
                    x = location.x,
                    y = location.y,
                    z = location.z,
                    iconName = config.integrations.dynmapSpawnIconName,
                    fallbackIconName = "world",
                    setId = "pcbridge_spawns",
                    setLabel = "Spawns",
                )
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to draw spawn markers" }
        }
    }
}
