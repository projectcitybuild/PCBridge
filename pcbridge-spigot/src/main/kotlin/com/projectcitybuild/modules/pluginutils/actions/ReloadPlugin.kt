package com.projectcitybuild.modules.pluginutils.actions

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.modules.chat.ChatGroupFormatter
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.libs.playercache.PlayerConfigCache
import com.projectcitybuild.repositories.WarpRepository

class ReloadPlugin(
    private val chatGroupFormatter: ChatGroupFormatter,
    private val playerConfigCache: PlayerConfigCache,
    private val warpRepository: WarpRepository,
    private val config: Config<ConfigData>,
) {
    fun execute() {
        chatGroupFormatter.flushAllCaches()
        playerConfigCache.flush()
        warpRepository.flush()
        config.flush()
    }
}
