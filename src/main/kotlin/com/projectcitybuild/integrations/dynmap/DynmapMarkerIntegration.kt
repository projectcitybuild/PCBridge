package com.projectcitybuild.integrations.dynmap

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotIntegration
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.plugin.events.WarpCreateEvent
import com.projectcitybuild.plugin.events.WarpDeleteEvent
import com.projectcitybuild.repositories.WarpRepository
import dagger.Reusable
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import org.dynmap.DynmapAPI
import javax.inject.Inject

@Reusable
class DynmapMarkerIntegration @Inject constructor(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
) : SpigotListener, SpigotIntegration {

    class DynmapAPINotFoundException : Exception("Dynmap plugin not found")
    class DynmapMarkerIconNotFoundException : Exception()

    companion object {
        private const val MARKER_SET_NAME = "pcbridge"
    }

    private var dynmap: DynmapAPI? = null

    override fun onEnable() {
        val anyPlugin = plugin.server.pluginManager.getPlugin("dynmap")
        if (anyPlugin == null) {
            logger.warning("Cannot find dynmap plugin. Disabling marker integration")
            return
        }
        if (anyPlugin !is DynmapAPI) {
            logger.fatal("Found dynmap plugin but cannot access dynmap-api")
            throw DynmapAPINotFoundException()
        }

        dynmap = anyPlugin
        plugin.server.pluginManager.registerEvents(this, plugin)

        updateWarpMarkers()
    }

    override fun onDisable() {
        if (dynmap != null) {
            dynmap = null
        }
    }

    @EventHandler fun onWarpCreate(event: WarpCreateEvent) = updateWarpMarkers()
    @EventHandler fun onWarpDelete(event: WarpDeleteEvent) = updateWarpMarkers()

    private fun updateWarpMarkers() {
        val dynmap = dynmap

        if (dynmap == null) {
            logger.verbose("Dynmap integration disabled. Skipping warp marker update")
            return
        }

        logger.verbose("Redrawing warp markers...")

        val markerAPI = dynmap.markerAPI
        val markerLabel = "Warps" // This name shows up in the Dynmap web interface

        val warpMarkerSet = markerAPI.getMarkerSet(MARKER_SET_NAME)
            ?: markerAPI.createMarkerSet(
                MARKER_SET_NAME,
                markerLabel,
                null, // TODO: what is this?
                false, // TODO: what is this?
            )

        warpMarkerSet.layerPriority = 1
        warpMarkerSet.hideByDefault = false

        warpMarkerSet.markers.forEach {
            it.deleteMarker()
        }

        val iconName = config.get(ConfigKey.INTEGRATION_DYNMAP_WARP_ICON)
        val icon = markerAPI.getMarkerIcon(iconName)
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