package com.projectcitybuild.modules.datetime

import dagger.Module
import dagger.Provides

@Module
class TimeProvider {

    @Provides
    fun provideTime(): Time {
        return LocalizedTime()
    }
}