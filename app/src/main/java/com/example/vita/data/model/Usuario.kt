package com.example.vita.data.model

data class Usuario(
    val nome: String = "",
    val email: String = "",
    val meta: String = "",
    val peso: String = "",
    val altura: String = "",
    val pesoMeta: String = "",
    val sexo: String = "",
    val nascimento: String = "",
    val nivelExercicio: String = "",
    val idr: Int = 0,
    val metaAgua: Int = 0
)