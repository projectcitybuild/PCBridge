package com.projectcitybuild.entities

sealed class SubChannel {

    companion object {
        const val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        const val AFK_END = "pcbridge:sub:afk-end"
        const val SET_WARP = "pcbridge:sub:set-warp"
    }
}