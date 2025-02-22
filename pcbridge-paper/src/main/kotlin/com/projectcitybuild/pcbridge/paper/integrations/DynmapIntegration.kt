package com.projectcitybuild.pcbridge.paper.integrations

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapCommonAPI
import org.dynmap.DynmapCommonAPIListener

class DynmapIntegration(
    private val plugin: JavaPlugin,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
) : Listener, DynmapCommonAPIListener() {
    private var dynmap: DynmapCommonAPI? = null

    suspend fun enable() {
        log.info { "Registering Dynmap listener..." }
        register(this)
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)

        updateWarpMarkers()
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
    suspend fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.integrations?.dynmapWarpIconName != next.integrations.dynmapWarpIconName) {
            updateWarpMarkers()
        }
    }

    private suspend fun updateWarpMarkers() {
        val dynmap = dynmap
        if (dynmap == null) {
            log.warn { "Dynmap integration disabled but attempted to draw warp markers" }
            return
        }

        log.debug { "Redrawing warp markers..." }

        val markerAPI = dynmap.markerAPI
        val warpMarkerSet = markerAPI.getMarkerSet(MARKER_SET_NAME)
            ?: markerAPI.createMarkerSet(
                MARKER_SET_NAME,    // Marker set ID
                "Warps",            // Marker set label (appears in dynmap web UI)
                null,               // Set of permitted marker icons
                false,              // Is marker set persistent
            )

        warpMarkerSet.apply {
            layerPriority = 1
            hideByDefault = false

            markers.forEach { it.deleteMarker() }
        }

        val config = remoteConfig.latest.config
        val iconName = config.integrations.dynmapWarpIconName
        val icon = markerAPI.getMarkerIcon(iconName)
            ?: markerAPI.getMarkerIcon("building").also {
                log.warn { "$iconName is not a valid dynmap icon name. Falling back to default icon" }
            }

        warpRepository.all().forEach { warp ->
            warpMarkerSet.createMarker(
                "warp_${warp.id}",  // Marker ID
                warp.name,          // Marker label
                false,              // Process label as HTML
                warp.world,         // World to display  marker in
                warp.x,             // x
                warp.y,             // y
                warp.z,             // z
                icon,               // Related MarkerIcon object
                false,              // Marker is persistent
            )
        }
    }

    private companion object {
        const val MARKER_SET_NAME = "pcbridge"
    }
}
