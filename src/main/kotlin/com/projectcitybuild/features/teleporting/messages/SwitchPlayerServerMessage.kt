package com.projectcitybuild.features.teleporting.messages

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.messages.MessagePipelineSerializable
import java.util.UUID

data class SwitchPlayerServerMessage(
    val playerUUID: UUID,
    val destinationServerName: String,
): MessagePipelineSerializable {

    override val subChannel = SubChannel.SWITCH_PLAYER_SERVER

    override fun serialize(stream: ByteArrayDataOutput) {
        stream.apply {
            writeUTF(playerUUID.toString())
            writeUTF(destinationServerName)
        }
    }

    companion object {
        fun deserialize(stream: ByteArrayDataInput): SwitchPlayerServerMessage {
            return SwitchPlayerServerMessage(
                playerUUID = UUID.fromString(stream.readUTF()),
                destinationServerName = stream.readUTF(),
            )
        }
    }
}