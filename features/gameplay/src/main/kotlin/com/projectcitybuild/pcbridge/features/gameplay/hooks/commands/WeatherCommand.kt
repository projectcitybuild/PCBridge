package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldWeatherResolution
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldWeatherService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandDefinition
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class WeatherCommand(
    plugin: Plugin,
    private val weatherService: WorldWeatherService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val worldArg = CommandArgument("world", StringArgumentType.word(), String::class.java)
    private val weatherArg = CommandArgument("weather", EnumArgument(WeatherMode::class.java), WeatherMode::class.java)
    private val durationArg = Argument.Integer("seconds", min = 0, max = MAX_DURATION_SECONDS)

    override val builder: CommandDefinition = command("weather") {
        description = l10n.gameplay.weatherCommandDescription
        requiresPermission(GameplayPermissions.WEATHER)
        argument(weatherArg) {
            argument(durationArg) {
                executesSuspending(taskExecutor) { setWeather(it, hasWorld = false) }
            }
            executesSuspending(taskExecutor) { setWeather(it, hasWorld = false) }
        }
        argument(worldArg) {
            argument(weatherArg) {
                argument(durationArg) {
                    executesSuspending(taskExecutor) { setWeather(it, hasWorld = true) }
                }
                executesSuspending(taskExecutor) { setWeather(it, hasWorld = true) }
            }
        }
    }

    private suspend fun setWeather(
        context: PaperCommandContext,
        hasWorld: Boolean,
    ) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val selector = if (hasWorld) worldArg.get(commandContext) else null
        val sender = commandContext.source.sender
        val world =
            when (val resolution = weatherService.resolveWorld(selector, (sender as? Player)?.world)) {
                is WorldWeatherResolution.Found -> resolution.world
                is WorldWeatherResolution.Missing -> {
                    sender.sendMessage(l10n.gameplay.errorWorldNotFound(resolution.name))
                    return@runCommandTracedSafely
                }
                WorldWeatherResolution.RequiresWorld -> {
                    sender.sendMessage(l10n.gameplay.errorWeatherWorldRequired)
                    return@runCommandTracedSafely
                }
            }
        val weather = weatherArg.get(commandContext)
        val seconds = durationArg.getOptional(commandContext)
        weatherService.set(world, weather == WeatherMode.STORM, seconds)
        commandContext.source.sender.sendMessage(
            l10n.gameplay.weatherSet(world.name, weather.name.lowercase(), seconds),
        )
    }

    private companion object {
        const val MAX_DURATION_SECONDS = Int.MAX_VALUE / 20
    }

    private enum class WeatherMode {
        SUN,
        STORM,
    }
}
