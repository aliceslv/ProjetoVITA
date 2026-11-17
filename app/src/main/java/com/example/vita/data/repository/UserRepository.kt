package com.example.vita.data.repository

import com.example.vita.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun usuarioReferencia(uid: String) =
        db.collection("usuarios").document(uid)

    fun salvarPerfil(
        uid: String,
        usuario: Usuario,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        usuarioReferencia(uid)
            .set(usuario)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun buscarPerfil(
        uid: String,
        resultado: (Usuario?) -> Unit,
        erro: (Exception) -> Unit
    ) {
        usuarioReferencia(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {
                    val usuario = documento.toObject(Usuario::class.java)
                    resultado(usuario)
                } else {
                    resultado(null)
                }
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }

    fun atualizarPerfil(
        uid: String,
        dados: Map<String, Any>,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        usuarioReferencia(uid)
            .update(dados)
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener { exception ->
                erro(exception)
            }
    }
}