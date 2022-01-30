package com.projectcitybuild.features.warps

import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.*
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import dagger.Module
import dagger.Provides
import org.bukkit.plugin.Plugin

@Module
class WarpUseCaseProvider {

    @Provides
    fun provideWarpListUseCase(
        warpRepository: WarpRepository,
        config: PlatformConfig,
    ): WarpListUseCase {
        return WarpListUseCaseImpl(
            warpRepository,
            config,
        )
    }

    @Provides
    fun provideWarpUseCase(
        plugin: Plugin,
        warpRepository: WarpRepository,
        queuedWarpRepository: QueuedWarpRepository,
        nameGuesser: NameGuesser,
        logger: PlatformLogger,
    ): WarpUseCase {
        return WarpUseCaseImpl(
            plugin,
            warpRepository,
            queuedWarpRepository,
            nameGuesser,
            logger,
        )
    }

    @Provides
    fun provideCreateWarpUseCase(
        warpRepository: WarpRepository,
    ): CreateWarpUseCase {
        return CreateWarpUseCaseImpl(warpRepository)
    }

    @Provides
    fun provideDeleteWarpUseCase(
        warpRepository: WarpRepository,
    ): DeleteWarpUseCase {
        return DeleteWarpUseCaseImpl(warpRepository)
    }
}