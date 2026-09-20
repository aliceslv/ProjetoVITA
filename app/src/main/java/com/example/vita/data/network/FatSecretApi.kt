package com.example.vita.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface FatSecretApi {

    @GET("api/alimentos/buscar")
    suspend fun buscarAlimentos(
        @Query("q") query: String,
    ): FoodSearchResponse
}