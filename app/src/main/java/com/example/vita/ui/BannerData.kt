package com.example.vita.ui

data class BannerData(
    val tipo: Int, // 1 para Banner 1, 2 para Banner 2
    val metaCalorias: Int,
    val consumidas: Int,
) {
    val restantes: Int
        get() = (metaCalorias - consumidas).coerceAtLeast(0)

    val porcentagem: Int
        get() = if (metaCalorias > 0) ((consumidas.toDouble() / metaCalorias) * 100).toInt().coerceIn(0, 100) else 0
}