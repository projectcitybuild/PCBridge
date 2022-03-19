package com.projectcitybuild.features.warps

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.cosmetics.adapters.gadgetsmenu.GadgetsMenuAdapter
import javax.inject.Inject

class CosmeticsModule @Inject constructor(
    private val gadgetsMenuAdapter: GadgetsMenuAdapter,
): SpigotFeatureModule {

    override fun onEnable() {
        gadgetsMenuAdapter.enable()
    }
}