package net.k1ra.succubotapp.features.appSettings.model.deviceManagement

import java.time.LocalDateTime

data class RobotInfo(
    val did: String,
    val ip: String,
    var mqttKey: String,
    var webKey: String,
    val lastPingAt: LocalDateTime
)