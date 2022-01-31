package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.config.PlatformConfig
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class WarpListUseCaseImpl @Inject constructor(
    private val warpRepository: WarpRepository,
    private val config: PlatformConfig
): WarpListUseCase {

    override fun getList(page: Int): WarpListUseCase.WarpList? {
        val warpsPerPage = config.get(PluginConfig.WARPS_PER_PAGE)
        val availableWarps = warpRepository.all().map { it.name }
        val totalWarpPages = ceil((availableWarps.size.toDouble() / warpsPerPage.toDouble())).toInt()

        if (availableWarps.isEmpty()) {
            return null
        }

        val currentPage = min(page, totalWarpPages)

        val warpList = availableWarps
            .sorted()
            .chunked(warpsPerPage)[max(currentPage - 1, 0)]

        return WarpListUseCase.WarpList(
            totalPages = totalWarpPages,
            currentPage = currentPage,
            warps = warpList,
        )
    }
}