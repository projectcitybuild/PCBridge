package com.projectcitybuild.entities

import java.time.LocalDateTime

data class Warp(
    val name: String,
    val serverName: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val createdAt: LocalDateTime,
)