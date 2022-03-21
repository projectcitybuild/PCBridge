package com.projectcitybuild.wiring.integrations.dynmap

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import javax.inject.Inject

class DynmapIntegrationModule @Inject constructor(
    private val dynmapMarkerAdapter: DynmapMarkerAdapter,
) : SpigotFeatureModule {

    override fun onEnable() {
        dynmapMarkerAdapter.enable()
    }
}