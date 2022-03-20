package com.projectcitybuild.modules.datetime.time

import java.time.LocalDateTime

interface Time {
    fun now(): LocalDateTime
}