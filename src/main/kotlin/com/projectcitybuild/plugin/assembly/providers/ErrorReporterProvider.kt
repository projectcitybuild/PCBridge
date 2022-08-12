package com.projectcitybuild.plugin.assembly.providers

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.errorreporting.adapters.SentryErrorReporter
import com.projectcitybuild.support.spigot.logger.Logger
import dagger.Module
import dagger.Provides

@Module
class ErrorReporterProvider {

    @Provides
    fun provideErrorReporter(
        config: Config,
        logger: Logger,
    ): ErrorReporter {
        return SentryErrorReporter(config, logger)
    }
}
