package com.projectcitybuild.modules.errorreporting

import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.errorreporting.adapters.SentryErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Module
import dagger.Provides

@Module
class ErrorReporterProvider {

    @Provides
    fun provideErrorReporter(
        config: ConfigKeys,
        logger: PlatformLogger,
    ): ErrorReporter {
        return SentryErrorReporter(config, logger)
    }
}
