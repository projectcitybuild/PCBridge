package com.projectcitybuild.plugin.assembly.providers

import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.modules.permissions.adapters.LuckPermsPermissions
import com.projectcitybuild.support.spigot.logger.PlatformLogger
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
