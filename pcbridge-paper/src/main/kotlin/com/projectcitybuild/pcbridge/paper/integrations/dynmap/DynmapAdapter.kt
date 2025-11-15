package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log

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

        val markerSet = markerAPI.getMarkerSet(setId)
            ?: markerAPI.createMarkerSet(
                setId,      // Marker set ID
                setLabel,   // Marker set label (appears in dynmap web UI)
                null,       // Set of permitted marker icons
                false,      // Is marker set persistent
            ).apply {
                layerPriority = 1
                hideByDefault = false
                markers.forEach { it.deleteMarker() }
            }.also {
                log.info { "Created new dynmap marker set: $setId" }
            }

        val icon = markerAPI.getMarkerIcon(iconName)
            ?: markerAPI.getMarkerIcon(fallbackIconName).also {
                log.warn { "$iconName is not a valid dynmap icon name. Falling back to default '$fallbackIconName' icon" }
            }
        markerSet.createMarker(
            id,         // Marker ID
            label,      // Marker label
            false,      // Process label as HTML
            world,      // World to display marker in
            x,          // x
            y,          // y
            z,          // z
            icon,       // Related MarkerIcon object
            false,      // Marker is persistent
        )
        log.info { "Created new dynmap marker: $id" }
    }
}
