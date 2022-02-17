package com.projectcitybuild.entities

enum class SubChannel {
    GLOBAL_CHAT,

    companion object {
        const val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        const val TP_SAME_SERVER = "pcbridge:sub:tp-same-server"
        const val TP_ACROSS_SERVER = "pcbridge:sub:tp-across-server"
        const val SWITCH_PLAYER_SERVER = "pcbridge:sub:switch-player-server"
    }
}