package net.k1ra.succubotapp.features.appSettings.model.serverManagement

data class LdapUserTestResponse(
    val elements: ArrayList<String> = arrayListOf(),
    var error: String? = null
)