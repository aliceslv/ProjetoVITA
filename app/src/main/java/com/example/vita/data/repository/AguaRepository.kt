package com.example.vita.data.repository

import com.example.vita.data.FirestorePaths
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class AguaRepository {

    private val db = FirebaseFirestore.getInstance()

    fun salvarRegistro(
        uid: String,
        quantidadeMl: Int,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {

        val dados = hashMapOf(
            "quantidade_ml" to quantidadeMl,
            "data" to Timestamp(Date())
        )

        db.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.AGUA)
            .add(dados)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun buscarRegistros(
        uid: String,
        resultado: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.AGUA)
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
}