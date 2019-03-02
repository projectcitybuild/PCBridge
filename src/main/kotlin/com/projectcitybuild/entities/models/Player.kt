package com.projectcitybuild.entities.models

import java.util.*

data class Player(val uuid: UUID,
                  var isMuted: Boolean,
                  var prefix: String?,
                  var suffix: String?)