package com.projectcitybuild.modules.errorreporting

import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.errorreporting.adapters.SentryErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Module
import dagger.Provides

@Module
class ErrorReporterProvider {

    @Provides
    fun provideErrorReporter(
        config: PlatformConfig,
        logger: PlatformLogger,
    ): ErrorReporter {
        return SentryErrorReporter(config, logger)
    }
}
