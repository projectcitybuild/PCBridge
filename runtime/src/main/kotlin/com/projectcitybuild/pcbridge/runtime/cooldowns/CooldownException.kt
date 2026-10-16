package com.projectcitybuild.pcbridge.runtime.cooldowns

import kotlin.time.Duration

class CooldownException(val remainingTime: Duration) : Exception()
