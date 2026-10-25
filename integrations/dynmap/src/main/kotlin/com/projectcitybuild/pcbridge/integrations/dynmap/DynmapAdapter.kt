package com.projectcitybuild.pcbridge.integrations.dynmap

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import java.util.UUID

class DynmapAdapter {
    private var delegate = DynmapDelegate()

    fun createMarker(
        id: String,
        label: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        iconName: String,
        fallbackIconName: String,
        setId: String,
        setLabel: String,
    ) {
        checkNotNull(delegate.instance) { "Dynmap unavailable" }

        val markerAPI = delegate.instance?.markerAPI
        checkNotNull(markerAPI) { "Marker API unavailable" }

        val markerSet =
            markerAPI.getMarkerSet(setId)
                ?: markerAPI.createMarkerSet(
                    setId,
                    setLabel,
                    null,
                    false,
                ).apply {
                    layerPriority = 1
                    hideByDefault = false
                    markers.forEach { it.deleteMarker() }
                }.also {
                    logSync.info { "Created new dynmap marker set: $setId" }
                }

        val icon =
            markerAPI.getMarkerIcon(iconName)
                ?: markerAPI.getMarkerIcon(fallbackIconName).also {
                    logSync.warn { "$iconName is not a valid dynmap icon name. Falling back to default '$fallbackIconName' icon" }
                }
        markerSet.createMarker(
            id,
            label,
            false,
            world,
            x,
            y,
            z,
            icon,
            false,
        )
        logSync.info { "Created new dynmap marker: $id" }
    }

    fun synchronizeBorderMarkers(
        borders: List<Triple<UUID, String, SerializableWorldBorder>>,
    ) {
        checkNotNull(delegate.instance) { "Dynmap unavailable" }
        val markerAPI = checkNotNull(delegate.instance?.markerAPI) { "Marker API unavailable" }
        val markerSet =
            markerAPI.getMarkerSet(BORDER_SET_ID)
                ?: markerAPI.createMarkerSet(BORDER_SET_ID, BORDER_SET_LABEL, null, false).apply {
                    layerPriority = 1
                    hideByDefault = false
                }.also {
                    logSync.info { "Created new dynmap marker set: $BORDER_SET_ID" }
                }

        val expectedIds = borders.mapTo(mutableSetOf()) { borderMarkerId(it.first) }
        markerSet.areaMarkers
            .filter { it.getMarkerID() !in expectedIds }
            .forEach { it.deleteMarker() }
        markerSet.circleMarkers
            .filter { it.getMarkerID() !in expectedIds }
            .forEach { it.deleteMarker() }

        borders.forEach { (worldId, worldName, border) ->
            val id = borderMarkerId(worldId)
            val label = "$worldName Border"
            if (border.effectiveShape == WorldBorderShape.CIRCLE) {
                markerSet.findAreaMarker(id)?.deleteMarker()
                val existing = markerSet.findCircleMarker(id)
                val marker =
                    if (existing != null && existing.world == worldName) {
                        existing.apply {
                            setLabel(label)
                            setCenter(worldName, border.centerX, 64.0, border.centerZ)
                            setRadius(border.radius, border.radius)
                        }
                    } else {
                        existing?.deleteMarker()
                        markerSet.createCircleMarker(
                            id,
                            label,
                            false,
                            worldName,
                            border.centerX,
                            64.0,
                            border.centerZ,
                            border.radius,
                            border.radius,
                            false,
                        )
                    }
                marker.setLineStyle(2, 1.0, BORDER_COLOR)
                marker.setFillStyle(0.08, BORDER_COLOR)
                return@forEach
            }

            markerSet.findCircleMarker(id)?.deleteMarker()
            val x =
                doubleArrayOf(
                    border.centerX - border.radius,
                    border.centerX + border.radius,
                    border.centerX + border.radius,
                    border.centerX - border.radius,
                )
            val z =
                doubleArrayOf(
                    border.centerZ - border.radius,
                    border.centerZ - border.radius,
                    border.centerZ + border.radius,
                    border.centerZ + border.radius,
                )
            val existing = markerSet.findAreaMarker(id)
            val marker =
                if (existing != null && existing.world == worldName) {
                    existing.apply {
                        setLabel(label)
                        setCornerLocations(x, z)
                    }
                } else {
                    existing?.deleteMarker()
                    markerSet.createAreaMarker(id, label, false, worldName, x, z, false)
                }

            marker.setLineStyle(2, 1.0, BORDER_COLOR)
            marker.setFillStyle(0.08, BORDER_COLOR)
        }
    }

    private fun borderMarkerId(worldId: UUID) = "world_border_$worldId"

    private companion object {
        const val BORDER_SET_ID = "pcbridge_world_borders"
        const val BORDER_SET_LABEL = "World Borders"
        const val BORDER_COLOR = 0xFF3B30
    }
}
