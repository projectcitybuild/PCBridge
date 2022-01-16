package com.projectcitybuild.modules.database

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.logger.LoggerProvider
import dagger.Module
import dagger.Provides
import net.md_5.bungee.api.plugin.Plugin

@Module
class DataSourceProvider {

    @Provides
    fun providesDataSource(
        plugin: Plugin,
        logger: LoggerProvider,
        config: ConfigProvider
    ): DataSource {
        return DataSource(
            plugin,
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