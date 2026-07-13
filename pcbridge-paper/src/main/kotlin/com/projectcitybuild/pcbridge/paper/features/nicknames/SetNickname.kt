package com.projectcitybuild.pcbridge.paper.features.nicknames

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player

class NicknameTooLongException: Exception()

class SetNickname {
    fun set(player: Player, nickname: String?) {
        if (nickname == null) {
            clear(player)
            return
        }
        val formatted = MiniMessage.miniMessage().deserialize(nickname)
        val length = plainTextLength(formatted)
        if (length > 30) {
            throw NicknameTooLongException()
        }
        player.displayName(formatted)
    }

    fun clear(player: Player) = player.displayName(null)

    private fun plainTextLength(component: Component): Int {
        val serializer = PlainTextComponentSerializer.plainText()
        val unformatted = serializer.serialize(component)
        return unformatted.length
    }
}