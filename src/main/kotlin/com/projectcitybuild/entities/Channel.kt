package com.projectcitybuild.entities

sealed class Channel {

    companion object {
        val BUNGEECORD = "pcbridge:bungeecord"
    }
}

sealed class SubChannel {

    companion object {
        val GLOBAL_CHAT = "pcbridge:sub:global-chat"
        val STAFF_CHAT = "pcbridge:sub:staff-chat"
        val AFK = "pcbridge:sub:afk"
    }
}