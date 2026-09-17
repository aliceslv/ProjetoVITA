package com.example.vita.data.network

import android.util.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FatSecretClient {

    private const val BASE_URL_TOKEN = "https://oauth.fatsecret.com/"
    private const val BASE_URL_API = "https://platform.fatsecret.com/"

    // Coloque suas credenciais reais aqui
    private const val CLIENT_ID = "9440afccb8b24a78b1455d680cdfcd67"
    private const val CLIENT_SECRET = "665e4fb2834c48d2b2efb8bab34664c6"

    private val retrofitToken: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_TOKEN)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitApi: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_API)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiTokenService: FatSecretApi = retrofitToken.create(FatSecretApi::class.java)
    val apiService: FatSecretApi = retrofitApi.create(FatSecretApi::class.java)

    fun getBasicAuthHeader(): String {
        val credentials = "$CLIENT_ID:$CLIENT_SECRET"
        val base64 = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $base64"
    }
}