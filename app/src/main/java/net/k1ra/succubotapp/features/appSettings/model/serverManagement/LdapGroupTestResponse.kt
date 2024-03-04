package net.k1ra.succubotapp.features.appSettings.model.serverManagement

data class LdapGroupTestResponse(
    val groups: ArrayList<String> = arrayListOf(),
    var isAdmin: Boolean = false,
    var error: String? = null
)