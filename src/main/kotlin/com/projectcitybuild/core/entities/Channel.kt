package com.projectcitybuild.core.entities

sealed class Channel {

    companion object {
        val BUNGEECORD = "pcbridge:bungeecord"
    }
}

sealed class SubChannel {

    companion object {
        val GLOBAL_CHAT = "pcbridge:sub:global-chat"
    }
}