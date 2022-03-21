package com.projectcitybuild.modules.permissions

import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.adapters.LuckPermsPermissions
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PermissionsProvider {

    @Singleton
    @Provides
    fun providePermissions(logger: PlatformLogger): Permissions {
        return LuckPermsPermissions(logger)
    }
}
