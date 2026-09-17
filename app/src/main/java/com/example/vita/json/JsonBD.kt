package com.example.vita.json

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JsonBD(private val context: Context) {

    private val fileName = "users.json"

    private fun getFile(): File {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }
        return file
    }

    fun getUsers(): JSONArray {
        val file = getFile()
        val jsonString = file.readText()
        return if (jsonString.isEmpty()) JSONArray("[]") else JSONArray(jsonString)
    }

    fun addUser(userId: String, nome: String, email: String, senha: String): Boolean {
        val users = getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.getString("email").equals(email, ignoreCase = true)) {
                return false // E-mail já existe
            }
        }

        val newUser = JSONObject().apply {
            put("id", userId)
            put("nome", nome)
            put("email", email)
            put("senha", senha)
        }

        users.put(newUser)
        getFile().writeText(users.toString(2))
        return true
    }

    // Função de gravação atualizada para aceitar NOME e SENHA
    fun salvarPerfilUsuario(
        nome: String,
        email: String,
        senha: String,
        meta: String,
        peso: String,
        altura: String,
        pesoMeta: String,
        sexo: String,
        nascimento: String,
        nivelExercicio: String,
        idr: Int
    ): Boolean {
        return try {
            val users = getUsers()
            var usuarioEncontrado = false

            for (i in 0 until users.length()) {
                val user = users.getJSONObject(i)
                if (user.optString("email").equals(email, ignoreCase = true)) {
                    // Atualiza o usuário existente no JSON com todas as informações do perfil
                    if (nome.isNotEmpty()) user.put("nome", nome)
                    if (senha.isNotEmpty()) user.put("senha", senha)
                    user.put("meta", meta)
                    user.put("peso", peso)
                    user.put("altura", altura)
                    user.put("pesoMeta", pesoMeta)
                    user.put("sexo", sexo)
                    user.put("nascimento", nascimento)
                    user.put("nivelExercicio", nivelExercicio)
                    user.put("idr", idr)
                    usuarioEncontrado = true
                    break
                }
            }

            // Se o usuário ainda não existia no JSON, cria o objeto completo
            if (!usuarioEncontrado) {
                val newUser = JSONObject().apply {
                    put("id", System.currentTimeMillis().toString())
                    put("nome", nome)
                    put("email", email)
                    put("senha", senha)
                    put("meta", meta)
                    put("peso", peso)
                    put("altura", altura)
                    put("pesoMeta", pesoMeta)
                    put("sexo", sexo)
                    put("nascimento", nascimento)
                    put("nivelExercicio", nivelExercicio)
                    put("idr", idr)
                }
                users.put(newUser)
            }

            getFile().writeText(users.toString(2))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun validateLogin(email: String, senha: String): Boolean {
        val users = getUsers()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.getString("email").equals(email, ignoreCase = true) &&
                user.getString("senha") == senha
            ) {
                return true
            }
        }
        return false
    }

    fun emailExists(email: String): Boolean {
        val users = getUsers()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.getString("email").equals(email, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}