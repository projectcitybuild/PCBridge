package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WeatherMode
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import kotlin.jvm.java

fun Argument.Companion.weather(name: String) = CommandArgument(
    name = name,
    type = EnumArgument(WeatherMode::class.java) {
        it.name.lowercase()
    },
    valueClass = WeatherMode::class.java,
)
