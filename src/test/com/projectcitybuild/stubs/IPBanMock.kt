package com.projectcitybuild.stubs

import com.projectcitybuild.entities.IPBan
import java.time.LocalDateTime

fun IPBanMock(ip: String? = null): IPBan {
    return IPBan(
        ip = ip ?: "127.0.0.1",
        bannerName = "banner_name",
        reason = "reason",
        createdAt = LocalDateTime.now(),
    )
}
