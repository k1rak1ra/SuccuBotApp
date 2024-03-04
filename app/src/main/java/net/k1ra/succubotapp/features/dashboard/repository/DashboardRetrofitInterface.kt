package net.k1ra.succubotapp.features.dashboard.repository

import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DashboardRetrofitInterface {

    @GET("robot/all")
    fun getRobotStatuses() : Call<List<RobotStatus>>

}