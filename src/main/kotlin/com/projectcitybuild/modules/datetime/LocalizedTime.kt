package com.projectcitybuild.modules.datetime

import java.time.LocalDateTime

class LocalizedTime: Time {
    override fun now(): LocalDateTime {
        return LocalDateTime.now()
    }
}