package com.example.vita.data.repository

import com.example.vita.data.FirestorePaths
import com.example.vita.ui.AlimentoConsumido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class RefeicaoRepository {

    private val db = FirebaseFirestore.getInstance()

    fun salvarRefeicao(
        uid: String,
        tipo: String,
        alimentos: List<AlimentoConsumido>,
        sucesso: (String) -> Unit,
        erro: (Exception) -> Unit
    ) {

        val referencia = db.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.REFEICOES)
            .document()

        val totalCalorias = alimentos.sumOf {
            it.calorias.toDouble()
        }

        val totalCarboidratos = alimentos.sumOf {
            it.carboidratos.toDouble()
        }

        val totalProteinas = alimentos.sumOf {
            it.proteinas.toDouble()
        }

        val totalGorduras = alimentos.sumOf {
            it.gorduras.toDouble()
        }

        val listaAlimentos = alimentos.map { alimento ->

            hashMapOf(
                "nome" to alimento.nome,
                "quantidade" to alimento.gramas.toDouble(),
                "unidade" to "g",
                "gramas" to alimento.gramas.toDouble(),
                "porcoes" to alimento.porcoes.toDouble(),
                "calorias" to alimento.calorias.toDouble(),
                "carboidratos" to alimento.carboidratos.toDouble(),
                "proteinas" to alimento.proteinas.toDouble(),
                "gorduras" to alimento.gorduras.toDouble(),
                "origem" to alimento.origem
            )
        }

        val dados = hashMapOf(
            "tipo" to tipo,
            "data" to Timestamp(Date()),
            "alimentos" to listaAlimentos,
            "total_calorias" to totalCalorias,
            "total_carboidratos" to totalCarboidratos,
            "total_proteinas" to totalProteinas,
            "total_gorduras" to totalGorduras
        )

        referencia.set(dados)
            .addOnSuccessListener {
                sucesso(referencia.id)
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun buscarRefeicoes(
        uid: String,
        resultado: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.REFEICOES)
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { consulta ->

                val lista = consulta.documents.mapNotNull { documento ->
                    documento.data?.toMutableMap()?.apply {
                        put("id", documento.id)
                    }
                }

                resultado(lista)
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun excluirRefeicao(
        uid: String,
        idRefeicao: String,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.REFEICOES)
            .document(idRefeicao)
            .delete()
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener {
                erro(it)
            }
    }
}