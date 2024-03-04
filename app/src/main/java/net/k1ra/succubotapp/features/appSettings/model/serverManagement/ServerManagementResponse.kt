package net.k1ra.succubotapp.features.appSettings.model.serverManagement

class ServerManagementResponse(
    var ldapUserTestResponse: LdapUserTestResponse? = null,
    var ldapGroupTestResponse: LdapGroupTestResponse? = null,
    var mqttIsConnected: Boolean,
    val mqttError: String?
)