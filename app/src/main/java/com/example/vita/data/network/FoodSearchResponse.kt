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

// Item individual do alimento
data class FoodItem(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_description") val foodDescription: String?,
    @SerializedName("food_type") val foodType: String?
)