package com.projectcitybuild.modules.errorreporting

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.errorreporting.adapters.SentryErrorReporter
import dagger.Module
import dagger.Provides

@Module
class ErrorReporterProvider {

    @Provides
    fun provideErrorReporter(config: PlatformConfig): ErrorReporter {
        return SentryErrorReporter(
            enabled = config.get(PluginConfig.ERROR_REPORTING_SENTRY_ENABLED),
            dsn = config.get(PluginConfig.ERROR_REPORTING_SENTRY_DSN),
        )
    }
}