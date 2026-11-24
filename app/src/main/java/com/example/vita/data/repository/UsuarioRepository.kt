package com.example.vita.data.repository

import com.example.vita.data.FirestorePaths
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()

    fun salvarPerfil(
        uid: String,
        dados: Map<String, Any>,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun buscarPerfil(
        uid: String,
        resultado: (Map<String, Any>?) -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->
                resultado(
                    if (documento.exists()) documento.data else null
                )
            }
            .addOnFailureListener {
                erro(it)
            }
    }

    fun atualizarPerfil(
        uid: String,
        dados: Map<String, Any>,
        sucesso: () -> Unit,
        erro: (Exception) -> Unit
    ) {
        db.collection(FirestorePaths.USERS)
            .document(uid)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                sucesso()
            }
            .addOnFailureListener {
                erro(it)
            }
    }
}