package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.events.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapCommonAPI
import org.dynmap.DynmapCommonAPIListener
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

class DynmapIntegration(
    private val plugin: JavaPlugin,
    private val spawnRepository: SpawnRepository,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
) : Listener, DynmapCommonAPIListener() {
    private var dynmap: DynmapCommonAPI? = null

    suspend fun enable() {
        log.info { "Registering Dynmap listener..." }
        register(this)
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)

        updateWarpMarkers()
        updateSpawnMarkers()
    }

    fun disable() {
        WarpCreateEvent.getHandlerList().unregister(this)
        WarpDeleteEvent.getHandlerList().unregister(this)
        dynmap = null

        log.info { "Dynmap integration disabled" }
    }

    override fun apiEnabled(p0: DynmapCommonAPI?) {
        log.trace { "apiEnabled called by dynmap" }

        if (p0 == null) {
            log.error { "Dynmap integration was passed a null API instance. Disabling integration..." }
            return
        }
        dynmap = p0
        log.info { "Dynmap integration enabled" }
    }

    override fun apiDisabled(api: DynmapCommonAPI?) {
        log.trace { "apiDisabled called by dynmap" }
        disable()
    }

    @EventHandler
    suspend fun onWarpCreate(event: WarpCreateEvent) = updateWarpMarkers()

    @EventHandler
    suspend fun onWarpDelete(event: WarpDeleteEvent) = updateWarpMarkers()

    @EventHandler
    suspend fun onSpawnUpdate(event: SpawnUpdatedEvent) = updateSpawnMarkers()

    @EventHandler
    suspend fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.integrations?.dynmapWarpIconName != next.integrations.dynmapWarpIconName) {
            updateWarpMarkers()
        }
        if (prev?.integrations?.dynmapSpawnIconName != next.integrations.dynmapSpawnIconName) {
            updateSpawnMarkers()
        }
    }

    private suspend fun updateWarpMarkers() = rebuildMarketSet(
        setId = "pcbridge_warps",
        setLabel = "Warps",
    ) { markerAPI, markerSet ->
        val config = remoteConfig.latest.config
        val iconName = config.integrations.dynmapWarpIconName
        val icon = markerAPI.getMarkerIcon(iconName)
            ?: markerAPI.getMarkerIcon("building").also {
                log.warn { "$iconName is not a valid dynmap icon name. Falling back to default icon" }
            }

        warpRepository.all().forEach { warp ->
            markerSet.createMarker(
                "warp_${warp.id}",  // Marker ID
                warp.name,          // Marker label
                false,              // Process label as HTML
                warp.world,         // World to display marker in
                warp.x,             // x
                warp.y,             // y
                warp.z,             // z
                icon,               // Related MarkerIcon object
                false,              // Marker is persistent
            )
        }
    }

    private suspend fun updateSpawnMarkers() = rebuildMarketSet(
        setId = "pcbridge_spawns",
        setLabel = "Spawns",
    ) { markerAPI, markerSet ->
        val config = remoteConfig.latest.config
        val iconName = config.integrations.dynmapSpawnIconName
        val icon = markerAPI.getMarkerIcon(iconName)
            ?: markerAPI.getMarkerIcon("world").also {
                log.warn { "$iconName is not a valid dynmap icon name. Falling back to default icon" }
            }

        spawnRepository.allLoaded().forEach { location ->
            markerSet.createMarker(
                "spawn_${location.world.uid}",  // Marker ID
                "Spawn",          // Marker label
                false,            // Process label as HTML
                location.world.name,  // World to display marker in
                location.x,       // x
                location.y,       // y
                location.z,       // z
                icon,             // Related MarkerIcon object
                false,            // Marker is persistent
            )
        }
    }

    private suspend fun rebuildMarketSet(
        setId: String,
        setLabel: String,
        block: suspend (MarkerAPI, MarkerSet) -> Unit,
    ) {
        val dynmap = dynmap
        if (dynmap == null) {
            log.warn { "Dynmap integration disabled but attempted to draw \"$setLabel\" markers" }
            return
        }

        log.debug { "Redrawing \"$setLabel\" markers..." }

        val markerAPI = dynmap.markerAPI
        val markerSet = markerAPI.getMarkerSet(setId)
            ?: markerAPI.createMarkerSet(
                setId,              // Marker set ID
                setLabel,           // Marker set label (appears in dynmap web UI)
                null,               // Set of permitted marker icons
                false,              // Is marker set persistent
            )

        markerSet.apply {
            layerPriority = 1
            hideByDefault = false

            markers.forEach { it.deleteMarker() }
        }
        block(markerAPI, markerSet)
    }
}
