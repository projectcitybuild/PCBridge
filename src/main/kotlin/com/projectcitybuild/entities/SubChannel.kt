package com.projectcitybuild.entities

sealed class SubChannel {

    companion object {
        const val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        const val AFK_END = "pcbridge:sub:afk-end"
        const val SET_WARP = "pcbridge:sub:set-warp"
        const val WARP_AWAIT_JOIN = "pcbridge:sub:warp-await"
        const val WARP_IMMEDIATELY = "pcbridge:sub:warp-immediately"
        const val SET_HUB = "pcbridge:sub:set-hub"
    }
}