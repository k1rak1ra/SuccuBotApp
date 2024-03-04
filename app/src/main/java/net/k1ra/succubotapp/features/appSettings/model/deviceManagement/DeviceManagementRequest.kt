package net.k1ra.succubotapp.features.appSettings.model.deviceManagement

data class DeviceManagementRequest(
    val did: String,
    val webKey: String,
    val mqttKey: String
)