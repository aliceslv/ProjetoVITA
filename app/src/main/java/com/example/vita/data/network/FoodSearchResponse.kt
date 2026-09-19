package com.example.vita.data.network

import com.google.gson.annotations.SerializedName

// Objeto da resposta completa da API
data class FoodSearchResponse(
    @SerializedName("foods") val foods: FoodsContainer?,
    @SerializedName("error") val error: FatSecretError?
)

data class FatSecretError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

// Container intermediário da API FatSecret
data class FoodsContainer(
    @SerializedName("food") val foodList: List<FoodItem>?
)

// Item individual do alimento atualizado com micronutrientes
data class FoodItem(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_description") val foodDescription: String?,
    @SerializedName("food_type") val foodType: String?,

    // Micronutrientes adicionados (valores padrão 0.0 caso o FatSecret não retorne)
    @SerializedName("fiber") val fiber: Float? = 0f,
    @SerializedName("sugar") val sugar: Float? = 0f,
    @SerializedName("sodium") val sodium: Float? = 0f,
    @SerializedName("cholesterol") val cholesterol: Float? = 0f,
    @SerializedName("saturated_fat") val saturatedFat: Float? = 0f
)