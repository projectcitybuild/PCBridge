package com.projectcitybuild.pcbridge.paper.core.libs.cooldowns

import kotlin.time.Duration

class CooldownException(val remainingTime: Duration): Exception()