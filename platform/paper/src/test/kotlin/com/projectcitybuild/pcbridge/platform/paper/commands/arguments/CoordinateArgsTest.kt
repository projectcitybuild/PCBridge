package com.projectcitybuild.pcbridge.platform.paper.commands.arguments

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.withCoordinateArgs
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class CoordinateArgsTest {
    private val source = mock<CommandSourceStack>()
    private val dispatcher = CommandDispatcher<CommandSourceStack>().apply {
        root.addChild(withCoordinateArgs(mock<Plugin>()) { _, _, _ -> }.build())
    }

    @Test
    fun `coordinates parse without rotation`() {
        val parse = dispatcher.parse("12.5 64 -8.25", source)

        assertTrue(parse.exceptions.isEmpty())
        assertFalse(parse.reader.canRead())
        val context = parse.context.build("12.5 64 -8.25")
        assertEquals(12.5, DoubleArgumentType.getDouble(context, "x"))
        assertEquals(64.0, DoubleArgumentType.getDouble(context, "y"))
        assertEquals(-8.25, DoubleArgumentType.getDouble(context, "z"))
        assertFalse(parse.context.arguments.containsKey("yaw"))
        assertFalse(parse.context.arguments.containsKey("pitch"))
    }

    @Test
    fun `rotation arguments can be supplied as yaw and then pitch`() {
        val yawOnly = dispatcher.parse("1 2 3 90", source)
        assertTrue(yawOnly.exceptions.isEmpty())
        assertFalse(yawOnly.reader.canRead())
        assertEquals(90.0f, FloatArgumentType.getFloat(yawOnly.context.build("1 2 3 90"), "yaw"))
        assertFalse(yawOnly.context.arguments.containsKey("pitch"))

        val yawAndPitch = dispatcher.parse("1 2 3 90 -15", source)
        assertTrue(yawAndPitch.exceptions.isEmpty())
        assertFalse(yawAndPitch.reader.canRead())
        val context = yawAndPitch.context.build("1 2 3 90 -15")
        assertEquals(90.0f, FloatArgumentType.getFloat(context, "yaw"))
        assertEquals(-15.0f, FloatArgumentType.getFloat(context, "pitch"))
    }

    @Test
    fun `incomplete or non-numeric coordinates do not parse as complete input`() {
        listOf(
            "1 2",
            "1 nope 3",
            "1 2 3 90 45 extra",
        ).forEach { input ->
            val parse = dispatcher.parse(input, source)

            assertTrue(
                parse.exceptions.isNotEmpty() ||
                    parse.reader.canRead() ||
                    parse.context.command == null,
                "Unexpectedly accepted: $input",
            )
        }
    }
}
