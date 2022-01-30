package com.projectcitybuild.features.teleporting

import com.projectcitybuild.features.teleporting.usecases.TPToggleUseCase
import com.projectcitybuild.features.teleporting.usecases.TPToggleUseCaseImpl
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import dagger.Module
import dagger.Provides

@Module
class TeleportingUseCaseProvider {

    @Provides
    fun provideTPToggleUseCase(
        playerConfigRepository: PlayerConfigRepository,
    ): TPToggleUseCase {
        return TPToggleUseCaseImpl(playerConfigRepository)
    }
}