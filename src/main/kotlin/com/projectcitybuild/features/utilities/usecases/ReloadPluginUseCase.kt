package com.projectcitybuild.features.utilities.usecases

import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import javax.inject.Inject

class ReloadPluginUseCase @Inject constructor(
    private val chatGroupFormatter: ChatGroupFormatter,
    private val playerConfigCache: PlayerConfigCache,
) {
    fun execute() {
        chatGroupFormatter.flushAllCaches()
        playerConfigCache.flush()
    }
}