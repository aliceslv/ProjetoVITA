package com.example.vita.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

object FirestoreData {

    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    //organizar conversao dos documentos do firestore em mapas
    private fun converterDocumentos(
        documentos: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<Map<String, Any>> {

        return documentos.mapNotNull { documento ->

            documento.data?.toMutableMap()?.apply {
                put("id", documento.id)
            }
        }
    }

    fun uid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun usuario(uid: String) =
        db.collection("users").document(uid)

    fun refeicoes(uid: String) =
        usuario(uid).collection("refeicoes")

    fun agua(uid: String) =
        usuario(uid).collection("agua")

    fun buscarPerfil(
        sucesso: (Map<String, Any>) -> Unit,
        erro: (Exception) -> Unit = {}
    ) {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return
        }

        usuario(id).get()
            .addOnSuccessListener { documento ->
                sucesso(documento.data ?: emptyMap())
            }
            .addOnFailureListener { erro(it) }
    }

    fun buscarRefeicoes(
        sucesso: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit = {}
    ) {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return
        }

        refeicoes(id)
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { consulta ->

                val lista = converterDocumentos(consulta.documents)

                sucesso(lista)
            }
            .addOnFailureListener { erro(it) }
    }

    fun buscarAgua(
        sucesso: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit = {}
    ) {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return
        }

        agua(id)
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { consulta ->

                val lista = converterDocumentos(consulta.documents)

                sucesso(lista)
            }
            .addOnFailureListener { erro(it) }
    }
    fun observarPerfil(
        sucesso: (Map<String, Any>) -> Unit,
        erro: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return null
        }

        return usuario(id).addSnapshotListener { documento, excecao ->
            if (excecao != null) {
                erro(excecao)
                return@addSnapshotListener
            }

            sucesso(documento?.data ?: emptyMap())
        }
    }

    fun observarRefeicoes(
        sucesso: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return null
        }

        return refeicoes(id)
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { consulta, excecao ->
                if (excecao != null) {
                    erro(excecao)
                    return@addSnapshotListener
                }

                val lista = converterDocumentos(
                    consulta?.documents ?: emptyList()
                )

                sucesso(lista)
            }
    }

    fun observarAgua(
        sucesso: (List<Map<String, Any>>) -> Unit,
        erro: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        val id = uid()

        if (id == null) {
            erro(IllegalStateException("Usuário não autenticado."))
            return null
        }

        return agua(id)
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { consulta, excecao ->
                if (excecao != null) {
                    erro(excecao)
                    return@addSnapshotListener
                }

                val lista = converterDocumentos(
                    consulta?.documents ?: emptyList()
                )

                sucesso(lista)
            }
    }

}