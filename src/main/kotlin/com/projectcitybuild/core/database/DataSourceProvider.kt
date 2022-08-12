package com.projectcitybuild.core.database

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.support.spigot.logger.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataSourceProvider {

    @Provides
    @Singleton
    fun providesDataSource(
        logger: Logger,
        config: Config
    ): DataSource {
        return DataSource(
            logger = logger,
            hostName = config.get(ConfigKeys.dbHostName),
            port = config.get(ConfigKeys.dbPort),
            databaseName = config.get(ConfigKeys.dbName),
            username = config.get(ConfigKeys.dbUsername),
            password = config.get(ConfigKeys.dbPassword),
            shouldRunMigrations = true
        )
    }
}
