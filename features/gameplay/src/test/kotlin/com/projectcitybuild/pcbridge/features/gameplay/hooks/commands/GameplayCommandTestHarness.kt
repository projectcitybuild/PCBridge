package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerWeatherService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldWeatherService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class GameplayCommandTestHarness(
    private val scope: TestScope,
) {
    val target = mock(Player::class.java)
    val sender = mock(Player::class.java)
    val server = mock(Server::class.java)
    val plugin = mock(Plugin::class.java)
    val executor = CoroutineScopeCommandTaskExecutor(scope)
    val playerTimeService = PlayerTimeService()
    val playerWeatherService = PlayerWeatherService()
    val worldTimeService = WorldTimeService(server)
    val worldWeatherService = WorldWeatherService(server)

    init {
        whenever(target.name).thenReturn("Alex")
        whenever(sender.name).thenReturn("Notch")
        whenever(sender.hasPermission(any<String>())).thenReturn(true)
        whenever(server.onlinePlayers).thenReturn(listOf(target, sender))
    }

    suspend fun execute(
        command: BrigadierCommand,
        input: String,
        commandSender: CommandSender = sender,
    ) {
        dispatcher(command).execute(input, source(commandSender))
        scope.runCurrent()
        scope.advanceUntilIdle()
    }

    fun assertParses(
        command: BrigadierCommand,
        input: String,
    ) {
        val parse = dispatcher(command).parse(input, source(sender))
        assertTrue(parse.exceptions.isEmpty(), "Could not parse: $input")
        assertFalse(parse.reader.canRead(), "Unparsed input remains: $input")
    }

    private fun dispatcher(command: BrigadierCommand) = CommandDispatcher<CommandSourceStack>().also {
        it.root.addChild(command.literal())
    }

    private fun source(sender: CommandSender): CommandSourceStack = mock(CommandSourceStack::class.java).also {
        whenever(it.sender).thenReturn(sender)
    }
}
