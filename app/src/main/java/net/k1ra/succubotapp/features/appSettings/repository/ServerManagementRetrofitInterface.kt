package net.k1ra.succubotapp.features.appSettings.repository

import net.k1ra.succubotapp.features.appSettings.model.serverManagement.CurrentServerConfiguration
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.ServerManagementRequest
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.ServerManagementResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ServerManagementRetrofitInterface {

    @GET("serverManagement")
    fun getCurrentServerConfiguration() : Call<CurrentServerConfiguration>

    @POST("serverManagement")
    fun updateServerSetting(
        @Body request: ServerManagementRequest
    ) : Call<ServerManagementResponse>
}