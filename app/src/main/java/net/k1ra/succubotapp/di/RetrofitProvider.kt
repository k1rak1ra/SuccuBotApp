package net.k1ra.succubotapp.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import net.k1ra.succubotapp.features.authentication.model.AuthenticationSettings
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object RetrofitProvider {

    @Provides
    fun provideOkHttpClient(
        appSettings: AuthenticationSettings,
        interceptor: Interceptor
    ) : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    fun provideRetrofit(
        gson: Gson,
        appSettings: AuthenticationSettings,
        okHttpClient: OkHttpClient
    ) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(appSettings.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            ).build()
    }
}