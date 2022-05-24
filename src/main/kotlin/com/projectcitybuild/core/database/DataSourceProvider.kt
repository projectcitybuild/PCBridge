package com.projectcitybuild.core.database

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.ConfigKeys
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
        config: ConfigKeys
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
