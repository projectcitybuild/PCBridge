package com.projectcitybuild.features.warps

import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.createwarp.CreateWarpUseCase
import com.projectcitybuild.features.warps.usecases.createwarp.CreateWarpUseCaseImpl
import com.projectcitybuild.features.warps.usecases.deletewarp.DeleteWarpUseCase
import com.projectcitybuild.features.warps.usecases.deletewarp.DeleteWarpUseCaseImpl
import com.projectcitybuild.features.warps.usecases.warp.WarpUseCase
import com.projectcitybuild.features.warps.usecases.warp.WarpUseCaseImpl
import com.projectcitybuild.features.warps.usecases.warplist.WarpListUseCase
import com.projectcitybuild.features.warps.usecases.warplist.WarpListUseCaseImpl
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.datetime.Time
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
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
        localEventBroadcaster: LocalEventBroadcaster,
    ): WarpUseCase {
        return WarpUseCaseImpl(
            plugin,
            warpRepository,
            queuedWarpRepository,
            nameGuesser,
            logger,
            localEventBroadcaster,
        )
    }

    @Provides
    fun provideCreateWarpUseCase(
        warpRepository: WarpRepository,
        time: Time,
    ): CreateWarpUseCase {
        return CreateWarpUseCaseImpl(warpRepository, time)
    }

    @Provides
    fun provideDeleteWarpUseCase(
        warpRepository: WarpRepository,
    ): DeleteWarpUseCase {
        return DeleteWarpUseCaseImpl(warpRepository)
    }
}