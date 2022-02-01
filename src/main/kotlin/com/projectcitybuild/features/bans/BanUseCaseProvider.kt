package com.projectcitybuild.features.bans

import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.features.bans.usecases.authconnection.AuthoriseConnectionUseCase
import com.projectcitybuild.features.bans.usecases.authconnection.AuthoriseConnectionUseCaseImpl
import com.projectcitybuild.features.bans.usecases.ban.BanUseCase
import com.projectcitybuild.features.bans.usecases.ban.BanUseCaseImpl
import com.projectcitybuild.features.bans.usecases.banip.BanIPUseCase
import com.projectcitybuild.features.bans.usecases.banip.BanIPUseCaseImpl
import com.projectcitybuild.features.bans.usecases.checkban.CheckBanUseCase
import com.projectcitybuild.features.bans.usecases.checkban.CheckBanUseCaseImpl
import com.projectcitybuild.features.bans.usecases.unban.UnbanUseCase
import com.projectcitybuild.features.bans.usecases.unban.UnbanUseCaseImpl
import com.projectcitybuild.features.bans.usecases.unbanip.UnbanIPUseCase
import com.projectcitybuild.features.bans.usecases.unbanip.UnbanIPUseCaseImpl
import com.projectcitybuild.modules.datetime.DateTimeFormatter
import com.projectcitybuild.modules.datetime.Time
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
        dateTimeFormatter: DateTimeFormatter,
    ): CheckBanUseCase {
        return CheckBanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            dateTimeFormatter,
        )
    }

    @Provides
    fun provideBanIPUseCase(
        ipBanRepository: IPBanRepository,
        playerKicker: PlayerKicker,
        time: Time,
    ): BanIPUseCase {
        return BanIPUseCaseImpl(
            ipBanRepository,
            playerKicker,
            time,
        )
    }

    @Provides
    fun provideUnbanIPUseCase(
        ipBanRepository: IPBanRepository,
    ): UnbanIPUseCase {
        return UnbanIPUseCaseImpl(ipBanRepository)
    }

    @Provides
    fun provideAuthoriseConnectionUseCae(
        banRepository: BanRepository,
        ipBanRepository: IPBanRepository,
    ): AuthoriseConnectionUseCase {
        return AuthoriseConnectionUseCaseImpl(
            banRepository,
            ipBanRepository,
        )
    }
}