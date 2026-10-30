package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.GameMode
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GameplayCommandsTest {
    private val sender = mock<Player>().also {
        whenever(it.name).thenReturn("Notch")
        whenever(it.hasPermission(any<String>())).thenReturn(true)
    }
    private val target = mock<Player>().also {
        whenever(it.name).thenReturn("Alex")
    }
    private val server = mock<Server>().also {
        whenever(it.onlinePlayers).thenReturn(listOf(sender, target))
    }
    private val plugin = mock<Plugin>()

    @Test
    fun `registers all gameplay aliases`() = runTest {
        val commands = commands(this)

        assertEquals(
            setOf("burn", "extinguish", "feed", "gamemode", "heal", "freeze", "kill", "purge", "suicide", "xp"),
            commands.map { it.literal().name }.toSet(),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `gameplay aliases execute their actions`() = runTest {
        val dispatcher = dispatcher(this)

        execute(dispatcher, "gamemode creative Alex")
        execute(dispatcher, "freeze Alex 7")
        execute(dispatcher, "xp 12 Alex")
        execute(dispatcher, "xp add 5 Alex")
        execute(dispatcher, "xp subtract 3 Alex")
        execute(dispatcher, "xp set 20 Alex")
        execute(dispatcher, "xp reset Alex")

        verify(target).gameMode = GameMode.CREATIVE
        verify(target).freezeTicks = 140
        verify(target).giveExp(12)
        verify(target).giveExp(5)
        verify(target).giveExp(-3)
        verify(target, times(2)).setExp(0f)
        verify(target, times(2)).setLevel(0)
        verify(target, times(2)).setTotalExperience(0)
        verify(target).giveExp(20)
        verify(sender).sendMessage(l10n.gameplay.playerExperienceAdded("Alex", 12, 0))
        verify(sender).sendMessage(l10n.gameplay.playerExperienceAdded("Alex", 5, 0))
        verify(sender).sendMessage(l10n.gameplay.playerExperienceSubtracted("Alex", 3, 0))
        verify(sender).sendMessage(l10n.gameplay.playerExperienceSet("Alex", 0))
        verify(sender).sendMessage(l10n.gameplay.playerExperienceReset("Alex"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `extinguish only changes players who are on fire`() = runTest {
        whenever(target.fireTicks).thenReturn(20)
        val dispatcher = dispatcher(this)

        execute(dispatcher, "extinguish Alex")

        verify(target).setFireTicks(0)
        verify(sender).sendMessage(l10n.gameplay.playerExtinguished("Alex"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `extinguish reports when the target is not on fire`() = runTest {
        val dispatcher = dispatcher(this)

        execute(dispatcher, "extinguish Alex")

        verify(target, never()).setFireTicks(any())
        verify(sender).sendMessage(l10n.gameplay.playerNotOnFire("Alex"))
    }

    @Test
    fun `commands accept expected argument forms`() = runTest {
        val dispatcher = dispatcher(this)
        val inputs = listOf(
            "burn",
            "burn Alex",
            "burn Alex 5",
            "burn 5",
            "extinguish Alex",
            "feed Alex",
            "gamemode creative Alex",
            "heal Alex",
            "freeze Alex 10",
            "freeze 10",
            "kill Alex",
            "purge Alex",
            "suicide",
            "xp 12 Alex",
            "xp set 12",
            "xp set 12 Alex",
            "xp add 12",
            "xp add 12 Alex",
            "xp subtract 12",
            "xp subtract 12 Alex",
            "xp reset",
            "xp reset Alex",
        )

        inputs.forEach { input ->
            val parse = dispatcher.parse(input, source())
            assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
            assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
        }
    }

    private fun commands(scope: TestScope): List<BrigadierCommand> {
        val executor = CoroutineScopeCommandTaskExecutor(scope)
        val subcommands =
            listOf(
                BurnCommand(plugin, server, executor),
                ExtinguishCommand(plugin, server, executor),
                FeedCommand(plugin, server, executor),
                GameModeCommand(plugin, server, executor),
                HealCommand(plugin, server, executor),
                FreezeCommand(plugin, server, executor),
                KillCommand(plugin, server, executor),
                PurgeCommand(plugin, server, executor),
                SuicideCommand(plugin, executor),
                XpCommand(plugin, server, executor),
            )
        return subcommands
    }

    private fun dispatcher(scope: TestScope) = CommandDispatcher<CommandSourceStack>().apply {
        commands(scope).forEach { root.addChild(it.literal()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun TestScope.execute(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        input: String,
    ) {
        dispatcher.execute(input, source())
        runCurrent()
    }

    private fun source() = mock<CommandSourceStack>().also {
        whenever(it.sender).thenReturn(sender)
    }
}
