package com.example.vita

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