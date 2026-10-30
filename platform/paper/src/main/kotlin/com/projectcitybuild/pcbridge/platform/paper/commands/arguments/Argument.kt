package com.projectcitybuild.pcbridge.platform.paper.commands.arguments

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerArgument
import org.bukkit.Server
import org.bukkit.entity.Player

class Argument private constructor() {
    class Integer(
        name: String,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE,
    ) : CommandArgument<Int>(
            name = name,
            type = IntegerArgumentType.integer(min, max),
            valueClass = Int::class.javaObjectType,
        )

    class OnlinePlayer(
        name: String,
        server: Server,
    ) : CommandArgument<Player>(
            name = name,
            type = OnlinePlayerArgument(server),
            valueClass = Player::class.java,
        )

    class GameMode(
        name: String,
    ) : CommandArgument<org.bukkit.GameMode>(
            name = name,
            type = EnumArgument(org.bukkit.GameMode::class.java),
            valueClass = org.bukkit.GameMode::class.java,
        )
}
