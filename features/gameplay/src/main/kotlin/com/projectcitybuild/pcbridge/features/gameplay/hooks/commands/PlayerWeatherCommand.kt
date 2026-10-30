package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerWeatherService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandDefinition
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.WeatherType
import org.bukkit.plugin.Plugin
import java.util.Locale

class PlayerWeatherCommand(
    plugin: Plugin,
    server: Server,
    private val playerWeatherService: PlayerWeatherService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)

    override val builder: CommandDefinition = command("pweather") {
        description = l10n.gameplay.playerWeatherCommandDescription
        requiresPermission(GameplayPermissions.PLAYER_WEATHER)
        executesSuspending(taskExecutor, ::showWeather)
        playerOperation("get", ::showWeather)
        playerOperation("reset") { resetWeather(it) }
        playerOperation("sun") { setWeather(it, WeatherType.CLEAR, "sun") }
        playerOperation("clear") { setWeather(it, WeatherType.CLEAR, "clear") }
        playerOperation("storm") { setWeather(it, WeatherType.DOWNFALL, "storm") }
        playerOperation("thunder") { setWeather(it, WeatherType.DOWNFALL, "thunder") }
    }

    private fun CommandBuilder.playerOperation(
        name: String,
        execute: suspend (PaperCommandContext) -> Unit,
    ) {
        literal(name) {
            argument(playerArg) {
                executesSuspending(taskExecutor, execute)
            }
            executesSuspending(taskExecutor, execute)
        }
    }

    private suspend fun showWeather(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        val weather = playerWeatherService.current(target)
        val message =
            if (weather == null) {
                l10n.gameplay.playerWeatherNormal(target.name)
            } else {
                l10n.gameplay.playerWeatherCurrent(target.name, weather.name.lowercase(Locale.ROOT))
            }
        commandContext.source.sender.sendMessage(message)
    }

    private suspend fun setWeather(
        context: PaperCommandContext,
        weather: WeatherType,
        label: String,
    ) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        playerWeatherService.set(target, weather)
        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerWeatherSet(target.name, label),
        )
    }

    private suspend fun resetWeather(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        playerWeatherService.reset(target)
        commandContext.source.sender.sendMessage(l10n.gameplay.playerWeatherReset(target.name))
    }
}
