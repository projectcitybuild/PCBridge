package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import org.bukkit.Server

class WorldSelectorArgument(
    name: String,
    server: Server,
) : CommandArgument<WorldSelector>(
        name = name,
        type = WorldSelectorArgumentParser(server),
        valueClass = WorldSelector::class.java,
    )
