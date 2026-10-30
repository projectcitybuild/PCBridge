package com.projectcitybuild.pcbridge.platform.paper.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

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
            override val builder = command("test") {
                description = "Runs a test command"
            }
        }

        assertEquals("test", brigadierCommand.literal().name)
        assertEquals("Runs a test command", brigadierCommand.description)
    }

    @Test
    fun `argument descriptor declares and retrieves a typed argument`() {
        val target = Argument.Integer("target")
        val value = Argument.Integer("value", min = 1, max = 20)
        var actualValue: Int? = null
        val definition = command("test") {
            argument(target) {
                argument(value) {
                    executes { context ->
                        actualValue = value.get(context)
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
        val dispatcher = CommandDispatcher<CommandSourceStack>().apply {
            root.addChild(definition.node)
        }

        dispatcher.execute("test 1 12", mock())

        assertEquals(12, actualValue)
    }

    @Test
    fun `argument descriptor returns null when optional argument is absent`() {
        val value = Argument.Integer("value")
        val actualValues = mutableListOf<Int?>()
        val definition = command("test") {
            executes { context ->
                actualValues += value.getOptional(context)
                Command.SINGLE_SUCCESS
            }
            argument(value) {
                executes { context ->
                    actualValues += value.getOptional(context)
                    Command.SINGLE_SUCCESS
                }
            }
        }
        val dispatcher = CommandDispatcher<CommandSourceStack>().apply {
            root.addChild(definition.node)
        }
        val source = mock<CommandSourceStack>()

        dispatcher.execute("test", source)
        dispatcher.execute("test 12", source)

        assertEquals(listOf(null, 12), actualValues)
    }
}
