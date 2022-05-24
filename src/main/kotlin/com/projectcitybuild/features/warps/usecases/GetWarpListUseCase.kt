package com.projectcitybuild.features.warps.usecases.warplist

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.repositories.WarpRepository
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class GetWarpListUseCase @Inject constructor(
    private val warpRepository: WarpRepository,
    private val config: Config
) {
    data class WarpList(
        val totalWarps: Int,
        val totalPages: Int,
        val currentPage: Int,
        val warps: List<String>,
    )

    fun getList(page: Int = 1): WarpList? {
        val warpsPerPage = config.keys.WARPS_PER_PAGE
        val availableWarps = warpRepository.names()
        val totalWarpPages = ceil((availableWarps.size.toDouble() / warpsPerPage.toDouble())).toInt()

        if (availableWarps.isEmpty()) {
            return null
        }

        val currentPage = min(page, totalWarpPages)

        val warpList = availableWarps
            .sorted()
            .chunked(warpsPerPage)[max(currentPage - 1, 0)]

        return WarpList(
            totalWarps = availableWarps.size,
            totalPages = totalWarpPages,
            currentPage = currentPage,
            warps = warpList,
        )
    }
}
