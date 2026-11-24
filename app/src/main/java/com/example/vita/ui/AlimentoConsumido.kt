package com.example.vita.ui

data class AlimentoConsumido(
    val nome: String,
    val gramas: Float,
    val porcoes: Float,
    val calorias: Float,
    val carboidratos: Float,
    val proteinas: Float,
    val gorduras: Float,
    val origem: String = "desconhecida"
)
