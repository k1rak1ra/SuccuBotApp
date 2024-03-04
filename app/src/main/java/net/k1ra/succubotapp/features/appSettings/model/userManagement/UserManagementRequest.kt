package net.k1ra.succubotapp.features.appSettings.model.userManagement

@Suppress("FORBIDDEN_VARARG_PARAMETER_TYPE", "UNUSED_PARAMETER")
class UserManagementRequest(
    vararg nothingsSoParamsByNameAreForced: Nothing,
    val passwordChange: String? = null,
    val uploadImage: String? = null,
    val changeName: String? = null,
    val changeEmail: String? = null,
    val changeAdminStatus: Boolean? = null,
    val logoutOutOfAll: Boolean? = null
)