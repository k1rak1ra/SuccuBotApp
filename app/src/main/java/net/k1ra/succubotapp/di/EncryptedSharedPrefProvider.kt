package net.k1ra.succubotapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.k1ra.succubotapp.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSharedPrefProvider @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPref = EncryptedSharedPreferences.create(
        Constants.EncryptedSharedPrefName, // fileName
        Constants.EncryptedSharedPrefKeyName, // masterKeyAlias
        context, // context
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // prefKeyEncryptionScheme
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // prefvalueEncryptionScheme
    )

    fun getStored(name: String, default: String): String {
        return sharedPref.getString(name, default)!!
    }

    fun getStoredNullable(name: String): String? {
        return sharedPref.getString(name, null)
    }

    fun setStored(name: String, value: String?) {
        sharedPref.edit().putString(name, value).apply()
    }
}