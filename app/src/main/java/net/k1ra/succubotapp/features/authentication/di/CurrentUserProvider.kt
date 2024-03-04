package net.k1ra.succubotapp.features.authentication.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.di.EncryptedSharedPrefProvider
import net.k1ra.succubotapp.features.authentication.model.User

@Module
@InstallIn(ViewModelComponent::class, FragmentComponent::class)
object CurrentUserProvider {

    @Provides
    fun provideCurrentUser(
        sharedpref: EncryptedSharedPrefProvider,
        gson: Gson
    ) : User? {
        val userJson = sharedpref.getStoredNullable(Constants.CurrentUserName)
        userJson ?: return null

        return gson.fromJson(userJson, User::class.java)
    }
}