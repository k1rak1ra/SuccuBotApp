package net.k1ra.succubotapp.features.appSettings.repository

import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.DeviceManagementRequest
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.DeviceManagementResponse
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.RobotInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DeviceManagementRetrofitInterface {

    @GET("deviceManagement")
    fun getCurrentDevices() : Call<List<RobotInfo>>

    @POST("deviceManagement")
    fun updateDeviceKeysAndTest(
        @Body request: DeviceManagementRequest
    ) : Call<DeviceManagementResponse>
}