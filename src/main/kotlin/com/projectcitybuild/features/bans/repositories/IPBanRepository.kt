package com.projectcitybuild.features.bans.repositories

import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.modules.database.DataSource
import javax.inject.Inject

class IPBanRepository @Inject constructor(
    private val dataSource: DataSource,
) {
    class IPAlreadyBanned : Exception()

    fun get(ip: String): IPBan? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM `ip_bans` WHERE `ip` = ?",
            ip,
        ) ?: return null

        return IPBan(
            ip = row.get("ip"),
            bannerName = row.get("banner_name"),
            reason = row.get("reason") ?: "",
            createdAt = row.get("created_at"),
        )
    }

    @Throws(IPAlreadyBanned::class)
    fun put(ipBan: IPBan) {
        val existingBan = get(ipBan.ip)
        if (existingBan != null) {
            throw IPAlreadyBanned()
        }

        dataSource.database().executeInsert(
            "INSERT INTO `ip_bans` VALUES (?, ?, ?, ?)",
            ipBan.ip,
            ipBan.bannerName,
            if (ipBan.reason.isEmpty()) null else ipBan.reason,
            ipBan.createdAt,
        )
    }
}