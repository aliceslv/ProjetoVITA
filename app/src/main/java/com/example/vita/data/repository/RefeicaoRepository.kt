package com.example.vita.data.repository

import com.example.vita.data.model.AlimentoConsumido
import com.example.vita.data.model.Refeicao
import com.google.firebase.firestore.FirebaseFirestore

class RefeicaoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun refeicoesReferencia(uid: String) =
        db.collection("usuarios")
            .document(uid)
            .collection("refeicoes")

    fun salvarRefeicao(
        uid: String,
        tipo: String,
        data: String,
        alimentos: List<AlimentoConsumido>,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        val totalCalorias = alimentos.sumOf { it.calorias.toDouble() }.toFloat()
        val totalCarboidratos = alimentos.sumOf { it.carboidratos.toDouble() }.toFloat()
        val totalProteinas = alimentos.sumOf { it.proteinas.toDouble() }.toFloat()
        val totalGorduras = alimentos.sumOf { it.gorduras.toDouble() }.toFloat()

        val refeicao = hashMapOf(
            "tipo" to tipo,
            "data" to data,
            "alimentos" to alimentos.map { alimento ->
                hashMapOf(
                    "nome" to alimento.nome,
                    "gramas" to alimento.gramas,
                    "porcoes" to alimento.porcoes,
                    "calorias" to alimento.calorias,
                    "carboidratos" to alimento.carboidratos,
                    "proteinas" to alimento.proteinas,
                    "gorduras" to alimento.gorduras
                )
            },
            "totalCalorias" to totalCalorias,
            "totalCarboidratos" to totalCarboidratos,
            "totalProteinas" to totalProteinas,
            "totalGorduras" to totalGorduras
        )

        refeicoesReferencia(uid)
            .add(refeicao)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun buscarRefeicoes(
        uid: String,
        resultado: (List<Refeicao>) -> Unit,
        erro: (Exception) -> Unit
    ) {
        refeicoesReferencia(uid)
            .get()
            .addOnSuccessListener { documentos ->

                val lista = documentos.map { documento ->

                    val alimentosSalvos =
                        documento.get("alimentos") as? List<Map<String, Any>> ?: emptyList()

                    val alimentos = alimentosSalvos.map { item ->
                        AlimentoConsumido(
                            nome = item["nome"] as? String ?: "",
                            gramas = (item["gramas"] as? Number)?.toFloat() ?: 0f,
                            porcoes = (item["porcoes"] as? Number)?.toFloat() ?: 0f,
                            calorias = (item["calorias"] as? Number)?.toFloat() ?: 0f,
                            carboidratos = (item["carboidratos"] as? Number)?.toFloat() ?: 0f,
                            proteinas = (item["proteinas"] as? Number)?.toFloat() ?: 0f,
                            gorduras = (item["gorduras"] as? Number)?.toFloat() ?: 0f
                        )
                    }

                    Refeicao(
                        id = documento.id,
                        tipo = documento.getString("tipo") ?: "",
                        data = documento.getString("data") ?: "",
                        alimentos = alimentos,
                        totalCalorias =
                            (documento.get("totalCalorias") as? Number)?.toFloat() ?: 0f,
                        totalCarboidratos =
                            (documento.get("totalCarboidratos") as? Number)?.toFloat() ?: 0f,
                        totalProteinas =
                            (documento.get("totalProteinas") as? Number)?.toFloat() ?: 0f,
                        totalGorduras =
                            (documento.get("totalGorduras") as? Number)?.toFloat() ?: 0f
                    )
                }

                resultado(lista)
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun excluirRefeicao(
        uid: String,
        idRefeicao: String,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        refeicoesReferencia(uid)
            .document(idRefeicao)
            .delete()
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }
}