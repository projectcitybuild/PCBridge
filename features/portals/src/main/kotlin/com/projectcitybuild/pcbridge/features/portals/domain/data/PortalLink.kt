package com.projectcitybuild.pcbridge.features.portals.domain.data

enum class PortalLinkType(
    val configKey: String,
) {
    NETHER("nether"),
    END("end"),
}

data class PortalLink(
    val sourceWorld: String,
    val type: PortalLinkType,
    val destinationWorld: String,
)

data class PortalTarget(
    val worldName: String,
    val explicit: Boolean,
)
