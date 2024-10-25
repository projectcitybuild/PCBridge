package com.projectcitybuild.pcbridge.integrations

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.features.warps.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.spigot.SpigotIntegration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapAPI

class DynmapIntegration(
    private val plugin: JavaPlugin,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
    sentry: SentryReporter,
) : Listener, SpigotIntegration(
        pluginName = "dynmap",
        pluginManager = plugin.server.pluginManager,
        sentry = sentry,
    ) {
    class DynmapMarkerIconNotFoundException : Exception()

    private var dynmap: DynmapAPI? = null

    override suspend fun onEnable(loadedPlugin: Plugin) {
        check(loadedPlugin is DynmapAPI) {
            "Found dynmap plugin but cannot cast to DynmapAPI class"
        }
        dynmap = loadedPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        log.info { "Dynmap integration enabled" }

        updateWarpMarkers()
    }

    override suspend fun onDisable() {
        if (dynmap != null) {
            WarpCreateEvent.getHandlerList().unregister(this)
            WarpDeleteEvent.getHandlerList().unregister(this)

            dynmap = null
        }
    }

    @EventHandler
    suspend fun onWarpCreate(event: WarpCreateEvent) = updateWarpMarkers()

    @EventHandler
    suspend fun onWarpDelete(event: WarpDeleteEvent) = updateWarpMarkers()

    private suspend fun updateWarpMarkers() {
        val dynmap = dynmap
        if (dynmap == null) {
            log.warn { "Dynmap integration disabled but attempted to draw warp markers" }
            return
        }

        log.debug { "Redrawing warp markers..." }

        val markerAPI = dynmap.markerAPI

        val warpMarkerSet =
            markerAPI.getMarkerSet(MARKER_SET_NAME)
                ?: markerAPI.createMarkerSet(
                    MARKER_SET_NAME,
                    // Name that shows up in the dynmap web interface
                    "Warps",
                    // TODO: what is this?
                    null,
                    // TODO: what is this?
                    false,
                )
        warpMarkerSet.apply {
            layerPriority = 1
            hideByDefault = false

            markers.forEach { it.deleteMarker() }
        }

        val config = remoteConfig.latest.config
        val iconName = config.integrations.dynmapWarpIconName
        val icon =
            markerAPI.getMarkerIcon(iconName)
                ?: throw DynmapMarkerIconNotFoundException()

        warpRepository.all(limit = 0).items.forEach { warp ->
            warpMarkerSet.createMarker(
                "warp.${warp.name}",
                warp.name,
                warp.location.worldName,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                icon,
                // TODO: what is this?
                false,
            )
        }
    }

    private companion object {
        const val MARKER_SET_NAME = "pcbridge"
    }
}
