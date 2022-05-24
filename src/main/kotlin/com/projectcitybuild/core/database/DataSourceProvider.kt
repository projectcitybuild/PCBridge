package com.projectcitybuild.core.database

import com.projectcitybuild.modules.config.Config
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
        config: Config
    ): DataSource {
        return DataSource(
            logger = logger,
            hostName = config.keys.DB_HOSTNAME,
            port = config.keys.DB_PORT,
            databaseName = config.keys.DB_NAME,
            username = config.keys.DB_USERNAME,
            password = config.keys.DB_PASSWORD,
            shouldRunMigrations = true
        )
    }
}
