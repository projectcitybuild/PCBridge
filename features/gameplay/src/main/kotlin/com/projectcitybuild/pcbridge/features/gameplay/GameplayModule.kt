package com.projectcitybuild.pcbridge.features.gameplay

import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerWeatherService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldWeatherService
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.BurnCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.ExtinguishCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.FeedCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.FreezeCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.GameModeCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.HealCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.KillCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.PlayerTimeCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.PlayerWeatherCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.PurgeCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.SuicideCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.TimeCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.WeatherCommand
import com.projectcitybuild.pcbridge.features.gameplay.hooks.commands.XpCommand
import com.projectcitybuild.pcbridge.runtime.features.paperFeature
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val gameplayModule =
    module {
        paperFeature("gameplay") {
            commands(
                get<BurnCommand>(),
                get<ExtinguishCommand>(),
                get<FeedCommand>(),
                get<GameModeCommand>(),
                get<HealCommand>(),
                get<FreezeCommand>(),
                get<KillCommand>(),
                get<PurgeCommand>(),
                get<SuicideCommand>(),
                get<XpCommand>(),
                get<TimeCommand>(),
                get<PlayerTimeCommand>(),
                get<WeatherCommand>(),
                get<PlayerWeatherCommand>(),
            )
        }

        factoryOf(::BurnCommand)
        factoryOf(::ExtinguishCommand)
        factoryOf(::FeedCommand)
        factoryOf(::GameModeCommand)
        factoryOf(::HealCommand)
        factoryOf(::FreezeCommand)
        factoryOf(::KillCommand)
        factoryOf(::PurgeCommand)
        factoryOf(::SuicideCommand)
        factoryOf(::XpCommand)
        factoryOf(::PlayerTimeService)
        factoryOf(::PlayerWeatherService)
        factoryOf(::WorldTimeService)
        factoryOf(::WorldWeatherService)
        factoryOf(::TimeCommand)
        factoryOf(::PlayerTimeCommand)
        factoryOf(::WeatherCommand)
        factoryOf(::PlayerWeatherCommand)
    }
