package com.projectcitybuild.integrations.plugins.gadgetsmenu

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import javax.inject.Inject

class GadgetsMenuIntegrationModule @Inject constructor(
    private val gadgetsMenuAdapter: GadgetsMenuAdapter,
) : SpigotFeatureModule {

    override fun onEnable() {
        gadgetsMenuAdapter.enable()
    }
}
