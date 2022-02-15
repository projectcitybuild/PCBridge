package com.projectcitybuild.core.infrastructure.database

import com.projectcitybuild.modules.config.ConfigKeys
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
            hostName = config.get(ConfigKeys.DB_HOSTNAME),
            port = config.get(ConfigKeys.DB_PORT),
            databaseName = config.get(ConfigKeys.DB_NAME),
            username = config.get(ConfigKeys.DB_USERNAME),
            password = config.get(ConfigKeys.DB_PASSWORD),
            shouldRunMigrations = true
        )
    }
}