package com.example.vita.util

import java.util.Locale

object UnidadeConverter {

    /**
     * Tenta identificar o valor numérico e a unidade na string da descrição da API e converte para gramas (ou ml equivalente).
     * Caso não consiga identificar a unidade, retorna [defaultGramas] (padrão: 100g).
     */
    fun extrairGramasDaDescricao(texto: String, defaultGramas: Float = 100f): Float {
        if (texto.isBlank()) return defaultGramas

        val textoMinusculo = texto.lowercase(Locale.ROOT)

        // 1. Onças Fluidas (fl oz / fluid oz) -> ~29.57g
        val regexFlOz = Regex("""(\d+(?:[.,]\d+)?)\s*(?:fl\s*oz|fluid\s*ounce|fluid\s*oz)""")
        regexFlOz.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 29.5735f
        }

        // 2. Onças de massa (oz / ounce / ounces)
        val regexOz = Regex("""(\d+(?:[.,]\d+)?)\s*(?:oz|ounce|ounces)""")
        regexOz.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 28.3495f
        }

        // 3. Libras (lb / lbs / pound / pounds) -> ~453.59g
        val regexLb = Regex("""(\d+(?:[.,]\d+)?)\s*(?:lb|lbs|pound|pounds)""")
        regexLb.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 453.592f
        }

        // 4. Quilogramas (kg) -> 1000g
        val regexKg = Regex("""(\d+(?:[.,]\d+)?)\s*(?:kg|kilo|kilogram)""")
        regexKg.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 1000f
        }

        // 5. Litros (l) -> 1000g
        val regexLitro = Regex("""(\d+(?:[.,]\d+)?)\s*(?:l|liter|liters|litro|litros)""")
        regexLitro.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 1000f
        }

        // 6. Xícaras / Cups -> ~240g
        val regexCup = Regex("""(\d+(?:[.,]\d+)?)\s*(?:cup|cups|xícara|xicara)""")
        regexCup.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 240f
        }

        // 7. Colher de Sopa (tbsp / tablespoon) -> ~15g
        val regexTbsp = Regex("""(\d+(?:[.,]\d+)?)\s*(?:tbsp|tablespoon|colher\s*de\s*sopa)""")
        regexTbsp.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 15f
        }

        // 8. Colher de Chá (tsp / teaspoon) -> ~5g
        val regexTsp = Regex("""(\d+(?:[.,]\d+)?)\s*(?:tsp|teaspoon|colher\s*de\s*chá|colher\s*de\s*cha)""")
        regexTsp.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor * 5f
        }

        // 9. Gramas e Mililitros diretos (g / ml) -> 1g
        val regexGramaMl = Regex("""(\d+(?:[.,]\d+)?)\s*(?:g|ml|gram|grams|grama|gramas)""")
        regexGramaMl.find(textoMinusculo)?.let { match ->
            val valor = parseValor(match.groupValues[1])
            if (valor > 0f) return valor
        }

        // Caso não encontre nenhuma unidade conhecida, retorna o padrão
        return defaultGramas
    }

    private fun parseValor(stringValor: String): Float {
        return stringValor.replace(",", ".").toFloatOrNull() ?: 0f
    }
}