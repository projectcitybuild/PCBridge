package com.projectcitybuild.pcbridge.paper.integrations.dynmap

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync

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
}
