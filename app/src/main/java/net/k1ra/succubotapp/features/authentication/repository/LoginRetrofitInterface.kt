package net.k1ra.succubotapp.features.authentication.repository

import net.k1ra.succubotapp.features.authentication.model.LoginRequest
import net.k1ra.succubotapp.features.authentication.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginRetrofitInterface {

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ) : Call<User>
}