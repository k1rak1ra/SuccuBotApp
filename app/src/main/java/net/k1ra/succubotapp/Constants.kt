package net.k1ra.succubotapp

import android.content.Intent

object Constants {
    const val EncryptedSharedPrefName = "Preferences"
    const val EncryptedSharedPrefKeyName = "PreferencesKey"
    const val AuthenticationSettingsName = "ApplicationSettings"
    const val AuthenticationSettingsDefault = "{ \"baseUrl\": \"https://example.net/succubot/\" }"
    const val CurrentUserName = "CurrentUser"
    const val UpdatedRobotStatusPassBackKey = "UpdatedRobotPassBack"
    const val UpdatedRobotSettingsPassBackKey = "UpdatedRobotSettingsPassBack"

    val LogoutIntent = Intent("net.k1ra.Logout").apply { `package` = "net.k1ra.succubotapp" }
}