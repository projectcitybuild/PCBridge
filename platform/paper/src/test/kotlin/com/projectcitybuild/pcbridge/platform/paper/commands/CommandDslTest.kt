package com.projectcitybuild.pcbridge.platform.paper.commands

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandDslTest {
    @Test
    fun `command description defaults to empty`() {
        assertEquals("", command("test").description)
    }

    @Test
    fun `command definition contains its root node and description`() {
        val definition = command("test") {
            description = "Runs a test command"
        }
        assertEquals("test", definition.node.name)
        assertEquals("Runs a test command", definition.description)
    }

    @Test
    fun `brigadier command exposes its DSL description and literal`() {
        val brigadierCommand = object : BrigadierCommand {
            override val build = command("test") {
                description = "Runs a test command"
            }
        }

        assertEquals("test", brigadierCommand.literal().name)
        assertEquals("Runs a test command", brigadierCommand.description)
    }
}
