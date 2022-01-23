package com.projectcitybuild.features.bans

import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.usecases.*
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import dagger.Module
import dagger.Provides

@Module
class BanUseCaseProvider {

    @Provides
    fun provideBanUseCase(
        banRepository: BanRepository,
        playerUUIDRepository: PlayerUUIDRepository,
        playerKicker: PlayerKicker,
        messageBroadcaster: MessageBroadcaster,
    ): BanUseCase {
        return BanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            playerKicker,
            messageBroadcaster,
        )
    }

    @Provides
    fun provideUnbanUseCase(
        banRepository: BanRepository,
        playerUUIDRepository: PlayerUUIDRepository,
        messageBroadcaster: MessageBroadcaster,
    ): UnbanUseCase {
        return UnbanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            messageBroadcaster,
        )
    }

    @Provides
    fun provideCheckBanUseCase(
        banRepository: BanRepository,
        playerUUIDRepository: PlayerUUIDRepository,
    ): CheckBanUseCase {
        return CheckBanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
        )
    }
}