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
import org.bukkit.entity.Player

class Argument private constructor() {
    class String(
        name: kotlin.String,
    ) : CommandArgument<kotlin.String>(
            name = name,
            type = StringArgumentType.string(),
            valueClass = kotlin.String::class.java,
        )

    class Word(
        name: kotlin.String,
    ) : CommandArgument<kotlin.String>(
            name = name,
            type = StringArgumentType.word(),
            valueClass = kotlin.String::class.java,
        )

    class GreedyString(
        name: kotlin.String,
    ) : CommandArgument<kotlin.String>(
            name = name,
            type = StringArgumentType.greedyString(),
            valueClass = kotlin.String::class.java,
        )

    class Integer(
        name: kotlin.String,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE,
    ) : CommandArgument<Int>(
            name = name,
            type = IntegerArgumentType.integer(min, max),
            valueClass = Int::class.javaObjectType,
        )

    class Float(
        name: kotlin.String,
        min: kotlin.Float = -kotlin.Float.MAX_VALUE,
        max: kotlin.Float = kotlin.Float.MAX_VALUE,
    ) : CommandArgument<kotlin.Float>(
            name = name,
            type = FloatArgumentType.floatArg(min, max),
            valueClass = kotlin.Float::class.javaObjectType,
        )

    class Double(
        name: kotlin.String,
        min: kotlin.Double = -kotlin.Double.MAX_VALUE,
        max: kotlin.Double = kotlin.Double.MAX_VALUE,
    ) : CommandArgument<kotlin.Double>(
            name = name,
            type = DoubleArgumentType.doubleArg(min, max),
            valueClass = kotlin.Double::class.javaObjectType,
        )

    class OnOff(
        name: kotlin.String,
    ) : CommandArgument<Boolean>(
            name = name,
            type = OnOffArgument(),
            valueClass = Boolean::class.javaObjectType,
        )

    class OnlinePlayerName(
        name: kotlin.String,
        server: Server,
    ) : CommandArgument<kotlin.String>(
            name = name,
            type = OnlinePlayerNameArgument(server),
            valueClass = kotlin.String::class.java,
        )

    class OnlinePlayer(
        name: kotlin.String,
        server: Server,
    ) : CommandArgument<Player>(
            name = name,
            type = OnlinePlayerArgument(server),
            valueClass = Player::class.java,
        )

    class Enum<T : kotlin.Enum<T>>(
        name: kotlin.String,
        enumClass: Class<T>,
    ) : CommandArgument<T>(
            name = name,
            type = EnumArgument(enumClass),
            valueClass = enumClass,
        )

    class World(
        name: kotlin.String,
    ) : CommandArgument<org.bukkit.World>(
            name = name,
            type = ArgumentTypes.world(),
            valueClass = org.bukkit.World::class.java,
        )

    class GameMode(
        name: kotlin.String,
    ) : CommandArgument<org.bukkit.GameMode>(
            name = name,
            type = EnumArgument(org.bukkit.GameMode::class.java),
            valueClass = org.bukkit.GameMode::class.java,
        )
}
