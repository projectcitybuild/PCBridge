package com.projectcitybuild.features.warps.adapters.dynmap

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.warps.events.WarpCreateEvent
import com.projectcitybuild.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import org.dynmap.DynmapAPI
import javax.inject.Inject

@Reusable
class DynmapMarkerAdapter @Inject constructor(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val logger: PlatformLogger,
): SpigotListener {
    class DynmapNotFoundException: Exception("Dynmap plugin not found")
    class DynmapMarkerIconNotFoundException: Exception()

    private val markerSetName = "pcbridge"
    private var isEnabled = false

    private lateinit var dynmap: DynmapAPI

    fun enable() {
        val anyPlugin = plugin.server.pluginManager.getPlugin("dynmap")
        if (anyPlugin == null) {
            logger.warning("Cannot find dynmap pluigin")
            throw DynmapNotFoundException()
        }
        if (anyPlugin !is DynmapAPI) {
            logger.fatal("Found dynmap plugin but cannot access dynmap-api")
            throw DynmapNotFoundException()
        }

        dynmap = anyPlugin
        plugin.server.pluginManager.registerEvents(this, plugin)

        isEnabled = true

        updateWarpMarkers()
    }

    @EventHandler fun onWarpCreate(event: WarpCreateEvent) = updateWarpMarkers()
    @EventHandler fun onWarpDelete(event: WarpDeleteEvent) = updateWarpMarkers()

    private fun updateWarpMarkers() {
        if (!isEnabled) {
            logger.verbose("Dynmap integration disabled. Skipping warp marker update")
            return
        }

        logger.verbose("Redrawing warp markers...")

        val markerAPI = dynmap.markerAPI

        val warpMarkerSet = markerAPI.getMarkerSet(markerSetName)
            ?: markerAPI.createMarkerSet(
                markerSetName,
                "warps",
                null, // TODO: what is this?
                false, // TODO: what is this?
            )

        warpMarkerSet.layerPriority = 1
        warpMarkerSet.hideByDefault = false

        warpMarkerSet.markers.forEach {
            it.deleteMarker()
        }

        val icon = markerAPI.getMarkerIcon("portal")
            ?: throw DynmapMarkerIconNotFoundException()

        warpRepository.all().forEach { warp ->
            warpMarkerSet.createMarker(
                "warp.${warp.name}",
                warp.name,
                warp.location.worldName,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                icon,
                false, // TODO: what is this?
            )
        }
    }
}