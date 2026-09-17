package com.example.vita.data.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FatSecretApi {

    // 1. Obter Token OAuth 2.0
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "basic"
    ): TokenResponse

    // 2. Buscar alimentos por nome (Substituído Any por FoodSearchResponse)
    @POST("rest/server.api")
    suspend fun buscarAlimentos(
        @Header("Authorization") bearerToken: String,
        @Query("method") method: String = "foods.search",
        @Query("search_expression") query: String,
        @Query("format") format: String = "json"
    ): FoodSearchResponse
}