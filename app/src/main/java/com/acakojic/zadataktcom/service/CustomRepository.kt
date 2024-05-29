package com.acakojic.zadataktcom.service

import android.content.Context
import android.util.Log
import com.acakojic.zadataktcom.data.LoginResponse
import com.acakojic.zadataktcom.data.Vehicle
import com.acakojic.zadataktcom.utility.EncryptedSharedPredManager
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class CustomRepository(context: Context) {

    private val BASE_URL = "https://zadatak.tcom.rs/zadatak/public/api/"

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val apiServiceWithoutBody: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(ApiService::class.java)
    }

    suspend fun login(email: String): Response<LoginResponse> {
        Log.i("CustomRepository", "POST /login")
        return apiService.login(mapOf("email" to email))
    }

    suspend fun getAllVehicles(context: Context): Response<List<Vehicle>> {
        Log.i("CustomRepository", "GET /allVehicles")
        val authToken = EncryptedSharedPredManager.getToken(context)
        return apiService.getAllVehicles("Bearer $authToken")
    }

    suspend fun addToFavorites(context: Context, vehicleID: Int): Result<Unit> {
        Log.i("CustomRepository", "POST /addToFavorites")

        val authToken = EncryptedSharedPredManager.getToken(context)
        try {
            val response = apiServiceWithoutBody.addToFavorites(
                authToken = "Bearer $authToken",
                vehicleID = vehicleID
            )
            if (response.isSuccessful) {
                return Result.success(Unit)
            } else {
                return Result.failure(RuntimeException("Response not successful"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getVehicleDetails(context: Context, vehicleID: Int): Response<Vehicle> {
        Log.i("CustomRepository", "GET /vehicle")
        val authToken = EncryptedSharedPredManager.getToken(context)
        return apiService.getVehicleDetails("Bearer $authToken", vehicleID)
    }
}


