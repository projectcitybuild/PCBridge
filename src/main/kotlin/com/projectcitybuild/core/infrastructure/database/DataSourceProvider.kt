package com.projectcitybuild.core.infrastructure.database

import com.projectcitybuild.modules.config.ConfigKey
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
            hostName = config.get(ConfigKey.DB_HOSTNAME),
            port = config.get(ConfigKey.DB_PORT),
            databaseName = config.get(ConfigKey.DB_NAME),
            username = config.get(ConfigKey.DB_USERNAME),
            password = config.get(ConfigKey.DB_PASSWORD),
            shouldRunMigrations = true
        )
    }
}
