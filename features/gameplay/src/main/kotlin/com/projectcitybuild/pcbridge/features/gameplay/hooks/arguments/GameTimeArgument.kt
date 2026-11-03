package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.projectcitybuild.pcbridge.features.gameplay.domain.data.GameTime
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument

class GameTimeArgument(
    name: String,
    allowFixedTime: Boolean = true,
    suggestionsAfterTime: () -> Collection<String> = { emptyList() },
) : CommandArgument<GameTime>(
        name = name,
        type = GameTimeArgumentParser(
            allowFixedTime = allowFixedTime,
            suggestionsAfterTime = suggestionsAfterTime,
        ),
        valueClass = GameTime::class.java,
    )
