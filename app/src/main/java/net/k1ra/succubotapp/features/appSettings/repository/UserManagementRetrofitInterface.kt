package net.k1ra.succubotapp.features.appSettings.repository

import net.k1ra.succubotapp.features.appSettings.model.userManagement.UserManagementRequest
import net.k1ra.succubotapp.features.authentication.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserManagementRetrofitInterface {

    @GET("userManagement/all")
    fun getCurrentUsers() : Call<List<User>>

    @POST("userManagement/{id}")
    fun updateUser(
        @Path("id") uid: String,
        @Body request: UserManagementRequest
    ) : Call<User>

    @DELETE("userManagement/{id}")
    fun deleteUser(
        @Path("id") uid: String
    ) : Call<Unit>
}