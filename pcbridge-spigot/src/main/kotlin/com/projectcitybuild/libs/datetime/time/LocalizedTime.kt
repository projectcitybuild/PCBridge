package com.projectcitybuild.libs.datetime.time

import java.time.LocalDateTime

class LocalizedTime : Time {
    override fun now(): LocalDateTime {
        return LocalDateTime.now()
    }
}
