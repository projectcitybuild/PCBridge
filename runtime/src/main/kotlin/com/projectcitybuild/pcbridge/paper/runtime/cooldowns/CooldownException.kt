package com.projectcitybuild.pcbridge.paper.runtime.cooldowns

import kotlin.time.Duration

class CooldownException(val remainingTime: Duration) : Exception()
