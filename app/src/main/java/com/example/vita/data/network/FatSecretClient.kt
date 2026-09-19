package com.example.vita.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FatSecretClient {

    // Substitua pela URL gerada pelo Render (mantenha a barra no final)
    private const val BASE_URL_PROXY = "https://fatsecret-proxy-bdof.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_PROXY)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: FatSecretApi = retrofit.create(FatSecretApi::class.java)
}