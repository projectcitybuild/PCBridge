package com.projectcitybuild.pcbridge.platform.paper.commands.arguments

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnOffArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerNameArgument
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.GameMode
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player

class Argument private constructor() {
    companion object {
        fun string(name: String) = CommandArgument(
            name = name,
            type = StringArgumentType.string(),
            valueClass = String::class.java,
        )

        fun word(name: String) = CommandArgument(
            name = name,
            type = StringArgumentType.word(),
            valueClass = String::class.java,
        )

        fun greedyString(name: String) = CommandArgument(
            name = name,
            type = StringArgumentType.greedyString(),
            valueClass = String::class.java,
        )

        fun integer(
            name: String,
            min: Int = Int.MIN_VALUE,
            max: Int = Int.MAX_VALUE,
        ) = CommandArgument(
            name = name,
            type = IntegerArgumentType.integer(min, max),
            valueClass = Int::class.javaObjectType,
        )

        fun float(
            name: String,
            min: Float = -Float.MAX_VALUE,
            max: Float = Float.MAX_VALUE,
        ) = CommandArgument(
            name = name,
            type = FloatArgumentType.floatArg(min, max),
            valueClass = Float::class.javaObjectType,
        )

        fun double(
            name: String,
            min: Double = -Double.MAX_VALUE,
            max: Double = Double.MAX_VALUE,
        ) = CommandArgument(
            name = name,
            type = DoubleArgumentType.doubleArg(min, max),
            valueClass = Double::class.javaObjectType,
        )

        fun onOff(name: String) = CommandArgument(
            name = name,
            type = OnOffArgument(),
            valueClass = Boolean::class.javaObjectType,
        )

        fun onlinePlayerName(
            name: String,
            server: Server,
        ) = CommandArgument(
            name = name,
            type = OnlinePlayerNameArgument(server),
            valueClass = String::class.java,
        )

        fun onlinePlayer(
            name: String,
            server: Server,
        ) = CommandArgument(
            name = name,
            type = OnlinePlayerArgument(server),
            valueClass = Player::class.java,
        )

        fun <T : Enum<T>> enumValue(
            name: String,
            enumClass: Class<T>,
            map: (T) -> String = { it.name },
        ) = CommandArgument(
            name = name,
            type = EnumArgument(enumClass, map),
            valueClass = enumClass,
        )

        fun world(name: String) = CommandArgument(
            name = name,
            type = ArgumentTypes.world(),
            valueClass = World::class.java,
        )
    }
}
