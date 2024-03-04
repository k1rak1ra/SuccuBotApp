package net.k1ra.succubotapp.features.authentication.di

import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.features.authentication.model.User
import okhttp3.Interceptor
import okhttp3.Request

@Module
@InstallIn(ViewModelComponent::class)
object InterceptorProvider {

    @Provides
    fun provideAuthInterceptor(
        currentUser: User?,
        @ApplicationContext context: Context
    ) : Interceptor {
        return Interceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${currentUser?.uid}:${currentUser?.token}")
                .build()

            val response = chain.proceed(newRequest)

            if (response.code() == 401) {
                context.sendBroadcast(Constants.LogoutIntent)
            }

            response
        }
    }
}