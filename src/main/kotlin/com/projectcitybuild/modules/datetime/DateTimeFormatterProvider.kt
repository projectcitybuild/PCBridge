package com.projectcitybuild.modules.datetime

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import dagger.Module
import dagger.Provides
import java.time.ZoneId
import java.util.*

@Module
class DateTimeFormatterProvider {

    @Provides
    fun provideDateTimeFormatter(config: PlatformConfig): DateTimeFormatter {
        return DateTimeFormatterImpl(
            locale = Locale.forLanguageTag(
                config.get(PluginConfig.TIME_LOCALE)
            ),
            timezone = ZoneId.of(
                config.get(PluginConfig.TIME_TIMEZONE)
            ),
        )
    }
}