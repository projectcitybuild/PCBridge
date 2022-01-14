package com.projectcitybuild.features.warps.repositories

import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.database.DataSource

class WarpRepository(
    private val dataSource: DataSource
) {
    fun exists(name: String): Boolean {
        return first(name) != null
    }

    fun first(name: String): Warp? {
        val statement = dataSource.connection().prepareStatement(
            "SELECT * FROM `warps` WHERE `name`='?'"
        )
        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            return Warp(
                name = resultSet.getString(1),
                serverName = resultSet.getString(2),
                worldName = resultSet.getString(3),
                x = resultSet.getDouble(4),
                y = resultSet.getDouble(5),
                z = resultSet.getDouble(6   ),
                pitch = resultSet.getFloat(7),
                yaw = resultSet.getFloat(8),
                createdAt = resultSet.getDate(9),
            )
        }
        resultSet.close()
        return null
    }

    fun all(): List<Warp> {
        val statement = dataSource.connection().prepareStatement(
            "SELECT * FROM `warps` ORDER BY `name` ASC"
        )
        val resultSet = statement.executeQuery()
        val warps = mutableListOf<Warp>()
        while (resultSet.next()) {
            warps.add(Warp(
                name = resultSet.getString(1),
                serverName = resultSet.getString(2),
                worldName = resultSet.getString(3),
                x = resultSet.getDouble(4),
                y = resultSet.getDouble(5),
                z = resultSet.getDouble(6),
                pitch = resultSet.getFloat(7),
                yaw = resultSet.getFloat(8),
                createdAt = resultSet.getDate(9),
            ))
        }
        resultSet.close()
        return warps
    }

    fun add(warp: Warp) {
        dataSource.connection().prepareStatement(
            "INSERT INTO `warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        ).apply {
            setString(1, warp.name)
            setString(2, warp.serverName)
            setString(3, warp.worldName)
            setDouble(4, warp.x)
            setDouble(5, warp.y)
            setDouble(6, warp.z)
            setFloat(7, warp.pitch)
            setFloat(8, warp.yaw)
            setDate(9, warp.createdAt)

            executeUpdate()
        }
    }

    fun delete(name: String) {
        dataSource.connection().prepareStatement(
            "DELETE FROM `warps` WHERE `name`='?'"
        ).apply {
            setString(1, name)
            executeUpdate()
        }
    }
}