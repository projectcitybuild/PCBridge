package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.IPBan
import javax.inject.Inject

class IPBanRepository @Inject constructor(
    private val dataSource: DataSource,
) {
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

    fun put(ipBan: IPBan) {
        dataSource.database().executeInsert(
            "INSERT INTO `ip_bans` VALUES (?, ?, ?, ?)",
            ipBan.ip,
            ipBan.bannerName,
            ipBan.reason.ifEmpty { null },
            ipBan.createdAt,
        )
    }

    fun delete(ip: String) {
        dataSource.database().executeUpdate(
            "DELETE FROM `ip_bans` WHERE `ip` = ?",
            ip,
        )
    }
}