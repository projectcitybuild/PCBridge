package com.projectcitybuild.features.utilities.usecases

import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.libs.config.Config
import com.projectcitybuild.libs.playercache.PlayerConfigCache
import com.projectcitybuild.repositories.WarpRepository

class ReloadPlugin(
    private val chatGroupFormatter: ChatGroupFormatter,
    private val playerConfigCache: PlayerConfigCache,
    private val warpRepository: WarpRepository,
    private val config: Config,
) {
    fun execute() {
        chatGroupFormatter.flushAllCaches()
        playerConfigCache.flush()
        warpRepository.flush()
        config.flush()
    }
}
