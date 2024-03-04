package net.k1ra.succubotapp.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.features.authentication.model.AuthenticationSettings

@Module
@InstallIn(ViewModelComponent::class, FragmentComponent::class)
object AuthenticationSettingsProvider {

    @Provides
    fun provideAuthenticationSettings(
        sharedpref: EncryptedSharedPrefProvider,
        gson: Gson
    ) : AuthenticationSettings {
        return gson.fromJson(sharedpref.getStored(
            Constants.AuthenticationSettingsName,
            Constants.AuthenticationSettingsDefault
        ), AuthenticationSettings::class.java)
    }
}