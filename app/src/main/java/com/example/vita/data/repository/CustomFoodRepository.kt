package com.example.vita.data.repository

import com.example.vita.data.FirestoreData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class CustomFoodRepository {

    private val db = FirebaseFirestore.getInstance()

    fun salvarAlimento(
        nome: String,
        calorias: Double,
        proteinas: Double,
        carboidratos: Double,
        gorduras: Double,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {

        val uid = FirestoreData.uid()

        if (uid == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return
        }

        val dados = hashMapOf(
            "nome" to nome.trim(),
            "calorias" to calorias,
            "proteinas" to proteinas,
            "carboidratos" to carboidratos,
            "gorduras" to gorduras,
            "data_criacao" to Timestamp.now()
        )

        db.collection("users")
            .document(uid)
            .collection("alimentos_personalizados")
            .add(dados)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun buscarAlimentos(
        sucesso: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit
    ) {

        val uid = FirestoreData.uid()

        if (uid == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return
        }

        db.collection("users")
            .document(uid)
            .collection("alimentos_personalizados")
            .get()
            .addOnSuccessListener { consulta ->

                val alimentos = consulta.documents.mapNotNull { documento ->

                    documento.data?.toMutableMap()?.apply {
                        put("id", documento.id)
                    }
                }

                sucesso(alimentos)
            }
            .addOnFailureListener {
                erro(it)
            }
    }
}
