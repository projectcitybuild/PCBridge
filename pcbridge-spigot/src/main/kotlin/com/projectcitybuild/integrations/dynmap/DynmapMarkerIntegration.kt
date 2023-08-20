package com.projectcitybuild.integrations.dynmap

import com.projectcitybuild.events.WarpCreateEvent
import com.projectcitybuild.events.WarpDeleteEvent
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.ConfigKeys
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.repositories.WarpRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.dynmap.DynmapAPI

class DynmapMarkerIntegration(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val config: Config,
    private val logger: PlatformLogger,
) : Listener, SpigotIntegration {

    class DynmapAPINotFoundException : Exception("Dynmap plugin not found")
    class DynmapMarkerIconNotFoundException : Exception()

    companion object {
        private const val MARKER_SET_NAME = "pcbridge"
    }

    private var dynmap: DynmapAPI? = null

    override fun onEnable() {
        try {
            val anyPlugin = plugin.server.pluginManager.getPlugin("dynmap")
            if (anyPlugin == null) {
                logger.warning("Cannot find dynmap plugin. Disabling marker integration")
                return
            }
            if (anyPlugin !is DynmapAPI) {
                logger.severe("Found dynmap plugin but cannot access dynmap-api")
                throw DynmapAPINotFoundException()
            }

            logger.info("dynmap integration enabled")

            dynmap = anyPlugin
            plugin.server.pluginManager.registerEvents(this, plugin)

            updateWarpMarkers()
        } catch (e: NoClassDefFoundError) {
            logger.warning("dynmap either missing or failed to load. Disabling marker integration")
        }
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

        val iconName = config.get(ConfigKeys.integrationDynmapWarpIcon)
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
