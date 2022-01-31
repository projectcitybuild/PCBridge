package com.projectcitybuild.features.warps.usecases.warplist

interface WarpListUseCase {
    data class WarpList(
        val totalPages: Int,
        val currentPage: Int,
        val warps: List<String>,
    )
    fun getList(page: Int = 1): WarpList?
}
