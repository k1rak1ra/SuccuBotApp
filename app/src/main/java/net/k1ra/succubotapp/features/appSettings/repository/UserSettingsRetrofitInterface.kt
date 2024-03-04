package net.k1ra.succubotapp.features.appSettings.repository

import net.k1ra.succubotapp.features.appSettings.model.UserRequest
import net.k1ra.succubotapp.features.authentication.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserSettingsRetrofitInterface {

    @POST("user")
    fun sendUserRequest(
        @Body request: UserRequest
    ) : Call<User>
}