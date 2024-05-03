package com.projectcitybuild.integrations

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.core.errors.SentryReporter
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.features.warps.events.WarpCreateEvent
import com.projectcitybuild.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.support.spigot.SpigotIntegration
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.DynmapAPI

class DynmapIntegration(
    private val plugin: JavaPlugin,
    private val warpRepository: WarpRepository,
    private val config: Config<PluginConfig>,
    private val sentry: SentryReporter,
    private val logger: PlatformLogger,
) : Listener, SpigotIntegration {

    class DynmapMarkerIconNotFoundException : Exception()

    private var dynmap: DynmapAPI? = null

    override suspend fun onEnable() = runCatching {
        val anyPlugin = plugin.server.pluginManager.getPlugin("dynmap")
        if (anyPlugin == null) {
            logger.warning("Cannot find dynmap plugin. Disabling marker integration")
            return
        }
        check (anyPlugin is DynmapAPI) {
            logger.severe("Found dynmap plugin but cannot access dynmap-api")
            "Cannot cast dynmap plugin to DynmapAPI"
        }
        dynmap = anyPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        logger.info("Dynmap integration enabled")
        updateWarpMarkers()
    }.onFailure {
        logger.severe("Failed to enable Dynmap integration: ${it.localizedMessage}")
        sentry.report(it)
    }.let {}

    override suspend fun onDisable() {
        if (dynmap != null) {
            WarpCreateEvent.getHandlerList().unregister(this)
            WarpDeleteEvent.getHandlerList().unregister(this)

            dynmap = null
        }
    }

    @EventHandler
    suspend fun onWarpCreate(event: WarpCreateEvent)
        = updateWarpMarkers()

    @EventHandler
    suspend fun onWarpDelete(event: WarpDeleteEvent)
        = updateWarpMarkers()

    private suspend fun updateWarpMarkers() {
        val dynmap = dynmap
        if (dynmap == null) {
            logger.warning("Dynmap integration disabled but attempted to draw warp markers")
            return
        }

        logger.verbose("Redrawing warp markers...")

        val markerAPI = dynmap.markerAPI

        val warpMarkerSet = markerAPI.getMarkerSet(MARKER_SET_NAME)
            ?: markerAPI.createMarkerSet(
                MARKER_SET_NAME,
                "Warps", // Name that shows up in the dynmap web interface
                null, // TODO: what is this?
                false, // TODO: what is this?
            )
        warpMarkerSet.apply {
            layerPriority = 1
            hideByDefault = false

            markers.forEach { it.deleteMarker() }
        }

        val iconName = config.get().integrations.dynmap.warpIconName
        val icon = markerAPI.getMarkerIcon(iconName)
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
                false, // TODO: what is this?
            )
        }
    }

    private companion object {
        const val MARKER_SET_NAME = "pcbridge"
    }
}
