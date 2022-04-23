package com.projectcitybuild.modules.datetime

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import com.projectcitybuild.modules.datetime.time.LocalizedTime
import com.projectcitybuild.modules.datetime.time.Time
import dagger.Module
import dagger.Provides
import java.time.ZoneId
import java.util.Locale

@Module
class DateTimeProvider {

    @Provides
    fun provideDateTimeFormatter(config: PlatformConfig): DateTimeFormatter {
        return DateTimeFormatterImpl(
            locale = Locale.forLanguageTag(
                config.get(ConfigKey.TIME_LOCALE)
            ),
            timezone = ZoneId.of(
                config.get(ConfigKey.TIME_TIMEZONE)
            ),
        )
    }

    @Provides
    fun provideTime(): Time {
        return LocalizedTime()
    }
}
