package com.projectcitybuild.entities

sealed class SubChannel {

    companion object {
        const val AFK_END = "pcbridge:sub:afk-end"
        const val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        const val SET_HUB = "pcbridge:sub:set-hub"
        const val TP_SAME_SERVER = "pcbridge:sub:tp-same-server"
        const val TP_ACROSS_SERVER = "pcbridge:sub:tp-across-server"
        const val SWITCH_PLAYER_SERVER = "pcbridge:sub:switch-player-server"
        const val WARP_SAME_SERVER = "pcbridge:sub:warp-same-server"
        const val WARP_ACROSS_SERVER = "pcbridge:sub:warp-across-server"
    }
}