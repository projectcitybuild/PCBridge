package com.projectcitybuild.features.utilities.usecases

import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import com.projectcitybuild.repositories.WarpRepository
import javax.inject.Inject

class ReloadPluginUseCase @Inject constructor(
    private val chatGroupFormatter: ChatGroupFormatter,
    private val playerConfigCache: PlayerConfigCache,
    private val warpRepository: WarpRepository,
) {
    fun execute() {
        chatGroupFormatter.flushAllCaches()
        playerConfigCache.flush()
        warpRepository.flush()
    }
}
