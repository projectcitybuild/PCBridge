package com.projectcitybuild.features.homes.usecases

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.repositories.HomeRepository
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class HomeListUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val config: PlatformConfig
) {
    data class HomeList(
        val totalPages: Int,
        val currentPage: Int,
        val homes: List<String>,
    )

    fun getList(playerUUID: UUID, page: Int = 1): HomeList? {
        val homesPerPage = config.get(ConfigKey.WARPS_PER_PAGE)
        val availableHomeNames = homeRepository.names(playerUUID)
        val totalPages = ceil((availableHomeNames.size.toDouble() / homesPerPage.toDouble())).toInt()

        if (availableHomeNames.isEmpty()) {
            return null
        }

        val currentPage = min(page, totalPages)

        val homeList = availableHomeNames
            .sorted()
            .chunked(homesPerPage)[max(currentPage - 1, 0)]

        return HomeList(
            totalPages = totalPages,
            currentPage = currentPage,
            homes = homeList,
        )
    }
}