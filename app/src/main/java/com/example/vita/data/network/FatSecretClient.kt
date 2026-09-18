package com.example.vita.data.network

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.vita.BuildConfig

object FatSecretClient {

    private const val BASE_URL_TOKEN = "https://oauth.fatsecret.com/"
    private const val BASE_URL_API = "https://platform.fatsecret.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofitToken: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_TOKEN)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitApi: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_API)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiTokenService: FatSecretApi = retrofitToken.create(FatSecretApi::class.java)
    val apiService: FatSecretApi = retrofitApi.create(FatSecretApi::class.java)

    fun getBasicAuthHeader(): String {
        val credentials = "${BuildConfig.FATSECRET_CLIENT_ID}:${BuildConfig.FATSECRET_CLIENT_SECRET}"
        val base64 = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $base64"
    }
}