package com.projectcitybuild.modules.datetime

import java.time.LocalDateTime

interface Time {
    fun now(): LocalDateTime
}