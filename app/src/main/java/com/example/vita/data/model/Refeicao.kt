package com.example.vita.data.model

data class Refeicao(
    val id: String = "",
    val tipo: String = "",
    val data: String = "",
    val alimentos: List<AlimentoConsumido> = emptyList(),
    val totalCalorias: Float = 0f,
    val totalCarboidratos: Float = 0f,
    val totalProteinas: Float = 0f,
    val totalGorduras: Float = 0f
)