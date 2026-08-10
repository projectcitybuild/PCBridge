package com.projectcitybuild.pcbridge.core.cooldowns

import kotlin.time.Duration

class CooldownNotExpiredException(val remainingTime: Duration): Exception()