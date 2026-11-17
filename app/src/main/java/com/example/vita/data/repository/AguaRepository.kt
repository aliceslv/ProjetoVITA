package com.example.vita.data.repository

import com.example.vita.data.model.RegistroAgua
import com.google.firebase.firestore.FirebaseFirestore

class AguaRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun aguaReferencia(uid: String) =
        db.collection("usuarios")
            .document(uid)
            .collection("agua")

    fun salvarRegistro(
        uid: String,
        data: String,
        quantidadeMl: Int,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        val registro = hashMapOf(
            "data" to data,
            "quantidadeMl" to quantidadeMl
        )

        aguaReferencia(uid)
            .add(registro)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun buscarRegistros(
        uid: String,
        data: String,
        resultado: (List<RegistroAgua>) -> Unit,
        erro: (Exception) -> Unit
    ) {
        aguaReferencia(uid)
            .whereEqualTo("data", data)
            .get()
            .addOnSuccessListener { documentos ->

                val lista = documentos.map { documento ->
                    RegistroAgua(
                        id = documento.id,
                        data = documento.getString("data") ?: "",
                        quantidadeMl =
                            (documento.get("quantidadeMl") as? Number)?.toInt() ?: 0
                    )
                }

                resultado(lista)
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun excluirRegistro(
        uid: String,
        idRegistro: String,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        aguaReferencia(uid)
            .document(idRegistro)
            .delete()
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }
}