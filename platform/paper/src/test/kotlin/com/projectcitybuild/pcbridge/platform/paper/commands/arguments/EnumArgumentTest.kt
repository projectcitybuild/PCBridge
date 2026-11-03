package com.projectcitybuild.pcbridge.platform.paper.commands.arguments

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.GameMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class EnumArgumentTest {
    @Test
    fun `enum mapping is used for parsing and suggestions`() {
        val mode = Argument.enumValue(
            name = "mode",
            enumClass = GameMode::class.java,
            map = { it.name.lowercase() },
        )
        var parsedMode: GameMode? = null
        val dispatcher = CommandDispatcher<CommandSourceStack>().apply {
            root.addChild(
                command("gamemode") {
                    argument(mode) {
                        executes { context ->
                            parsedMode = mode.get(context)
                            1
                        }
                    }
                }.node,
            )
        }
        val source = mock<CommandSourceStack>()
        val input = "gamemode survival"

        dispatcher.execute(input, source)

        assertEquals(GameMode.SURVIVAL, parsedMode)
        assertEquals(
            setOf("survival", "creative", "adventure", "spectator"),
            dispatcher.getCompletionSuggestions(dispatcher.parse("gamemode ", source)).get().list.map { it.text }.toSet(),
        )
        assertEquals(
            listOf("creative"),
            dispatcher.getCompletionSuggestions(dispatcher.parse("gamemode cre", source)).get().list.map { it.text },
        )
    }
}
