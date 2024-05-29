package com.acakojic.zadataktcom.service

import com.acakojic.zadataktcom.data.LoginResponse
import com.acakojic.zadataktcom.data.Vehicle
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Headers
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "x-api-key: vekq8ne97uryr3mj4iudv8um07ggmhcat874q96jzvyypabgrm3zhyrwcgybm4hk"
    )
    suspend fun login(@Body credentials: Map<String, String>): Response<LoginResponse>

    @GET("allVehicles")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "x-api-key: vekq8ne97uryr3mj4iudv8um07ggmhcat874q96jzvyypabgrm3zhyrwcgybm4hk"
    )
    suspend fun getAllVehicles(
        @Header("Authorization") authToken: String
    ): Response<List<Vehicle>>

    @POST("addToFavorites")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "x-api-key: vekq8ne97uryr3mj4iudv8um07ggmhcat874q96jzvyypabgrm3zhyrwcgybm4hk"
    )
    suspend fun addToFavorites(
        @Header("Authorization") authToken: String,
        @Query("vehicleID") vehicleID: Int
    ): Response<ResponseBody> // Expecting a raw response body

    @GET("vehicle")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "x-api-key: vekq8ne97uryr3mj4iudv8um07ggmhcat874q96jzvyypabgrm3zhyrwcgybm4hk"
    )
    suspend fun getVehicleDetails(
        @Header("Authorization") authToken: String,
        @Query("vehicleID") vehicleID: Int
    ): Response<Vehicle>

}