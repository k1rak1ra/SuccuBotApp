package net.k1ra.succubotapp.features.robotStatus.repository

import net.k1ra.succubotapp.features.robotStatus.model.RobotCommand
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RobotStatusRetrofitInterface {

    @GET("robot/{id}")
    fun getRobotStatus(
        @Path("id") id: String
    ) : Call<RobotStatus>

    @POST("robot/{id}")
    fun sendRobotCommand(
        @Path("id") id: String,
        @Body command: RobotCommand
    ) : Call<Unit>
}