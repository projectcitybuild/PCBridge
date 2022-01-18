package com.projectcitybuild.entities

sealed class SubChannel {

    companion object {
        const val AFK_END = "pcbridge:sub:afk-end"
        const val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        const val SET_WARP = "pcbridge:sub:set-warp"
        const val SET_HUB = "pcbridge:sub:set-hub"
        const val TP_IMMEDIATELY = "pcbridge:sub:tp-immediately"
        const val WARP_IMMEDIATELY = "pcbridge:sub:warp-immediately"
    }
}