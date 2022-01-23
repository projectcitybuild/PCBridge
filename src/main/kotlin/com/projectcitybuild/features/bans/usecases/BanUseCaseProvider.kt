package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.features.bans.repositories.BanRepository
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
}