package com.projectcitybuild.core.infrastructure.database

import com.projectcitybuild.modules.config.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataSourceProvider {

    @Provides
    @Singleton
    fun providesDataSource(
        logger: PlatformLogger,
        config: PlatformConfig
    ): DataSource {
        return DataSource(
            logger = logger,
            hostName = config.get(PluginConfig.DB_HOSTNAME),
            port = config.get(PluginConfig.DB_PORT),
            databaseName = config.get(PluginConfig.DB_NAME),
            username = config.get(PluginConfig.DB_USERNAME),
            password = config.get(PluginConfig.DB_PASSWORD),
            shouldRunMigrations = true
        )
    }
}