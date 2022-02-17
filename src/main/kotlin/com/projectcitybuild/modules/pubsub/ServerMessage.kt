package com.projectcitybuild.modules.pubsub

interface ServerMessage {
    fun serialize()
    fun deserialize()
}