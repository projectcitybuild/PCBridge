package com.projectcitybuild.entities

data class CrossServerLocation(
    val serverName: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
)