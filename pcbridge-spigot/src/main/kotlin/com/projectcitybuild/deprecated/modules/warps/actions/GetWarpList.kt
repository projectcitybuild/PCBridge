package com.projectcitybuild.modules.warps.actions

import com.projectcitybuild.core.config.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.repositories.WarpRepository
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class GetWarpList(
    private val warpRepository: WarpRepository,
    private val config: Config<PluginConfig>
) {
    data class WarpList(
        val totalWarps: Int,
        val totalPages: Int,
        val currentPage: Int,
        val warps: List<String>,
    )

    fun getList(page: Int = 1): WarpList? {
        val warpsPerPage = config.get().warps.itemsPerPage
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
