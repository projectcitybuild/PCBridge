package com.projectcitybuild.plugin.assembly.providers

import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatterImpl
import com.projectcitybuild.modules.datetime.time.LocalizedTime
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import dagger.Module
import dagger.Provides
import java.time.ZoneId
import java.util.Locale

@Module
class DateTimeProvider {

    @Provides
    fun provideDateTimeFormatter(config: Config): DateTimeFormatter {
        return DateTimeFormatterImpl(
            locale = Locale.forLanguageTag(
                config.get(ConfigKeys.timeLocale)
            ),
            timezone = ZoneId.of(
                config.get(ConfigKeys.timeTimezone)
            ),
        )
    }

    @Provides
    fun provideTime(): Time {
        return LocalizedTime()
    }
}
