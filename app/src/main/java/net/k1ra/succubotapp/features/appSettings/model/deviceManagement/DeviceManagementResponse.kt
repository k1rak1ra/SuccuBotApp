package net.k1ra.succubotapp.features.appSettings.model.deviceManagement

data class DeviceManagementResponse(
    var mqttKeyValid: Boolean,
    var webKeyValid: Boolean?
)