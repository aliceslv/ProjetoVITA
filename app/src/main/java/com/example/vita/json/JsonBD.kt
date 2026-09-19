package com.example.vita.json

import android.content.Context
import com.example.vita.data.model.AlimentoConsumido
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JsonBD(private val context: Context) {

    private val fileName = "users.json"
    private val fileRefeicoesName = "refeicoes.json"

    private fun getFile(): File {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }
        return file
    }

    private fun getFileRefeicoes(): File {
        val file = File(context.filesDir, fileRefeicoesName)
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

    // Converte datas para o formato ISO Padrão (YYYY-MM-DD)
    fun formatarDataParaIso(dataStr: String): String {
        return try {
            if (dataStr.contains("/")) {
                val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = parser.parse(dataStr)
                if (date != null) formatter.format(date) else dataStr
            } else {
                dataStr
            }
        } catch (e: Exception) {
            dataStr
        }
    }

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

            val nascimentoIso = formatarDataParaIso(nascimento)

            for (i in 0 until users.length()) {
                val user = users.getJSONObject(i)
                if (user.optString("email").equals(email, ignoreCase = true)) {
                    if (nome.isNotEmpty()) user.put("nome", nome)
                    if (senha.isNotEmpty()) user.put("senha", senha)
                    user.put("meta", meta)
                    user.put("peso", peso)
                    user.put("altura", altura)
                    user.put("pesoMeta", pesoMeta)
                    user.put("sexo", sexo)
                    user.put("nascimento", nascimentoIso)
                    user.put("nivelExercicio", nivelExercicio)
                    user.put("idr", idr)
                    usuarioEncontrado = true
                    break
                }
            }

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
                    put("nascimento", nascimentoIso)
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

    fun atualizarMedidasUsuario(
        email: String,
        novoPeso: String,
        novaAltura: String,
        novoPesoMeta: String
    ): Boolean {
        return try {
            val users = getUsers()
            var usuarioEncontrado = false

            for (i in 0 until users.length()) {
                val user = users.getJSONObject(i)
                if (user.optString("email").equals(email, ignoreCase = true)) {
                    user.put("peso", novoPeso)
                    user.put("altura", novaAltura)
                    user.put("pesoMeta", novoPesoMeta)
                    usuarioEncontrado = true
                    break
                }
            }

            if (usuarioEncontrado) {
                getFile().writeText(users.toString(2))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Salva uma refeição garantindo que a data seja gravada no padrão YYYY-MM-DD
    fun salvarRefeicao(
        emailUsuario: String,
        tipoRefeicao: String,
        data: String,
        alimentos: List<AlimentoConsumido>
    ): Boolean {
        return try {
            val file = getFileRefeicoes()
            val jsonArray = JSONArray(file.readText())

            val dataIso = formatarDataParaIso(data)

            val novaRefeicao = JSONObject().apply {
                put("id", System.currentTimeMillis().toString())
                put("usuarioEmail", emailUsuario)
                put("data", dataIso)
                put("tipo", tipoRefeicao)

                val arrayAlimentos = JSONArray()
                for (item in alimentos) {
                    val itemJson = JSONObject().apply {
                        put("nome", item.nome)
                        put("gramas", item.gramas)
                        put("porcoes", item.porcoes)
                        put("calorias", item.calorias)
                        put("carboidratos", item.carboidratos)
                        put("proteinas", item.proteinas)
                        put("gorduras", item.gorduras)
                    }
                    arrayAlimentos.put(itemJson)
                }
                put("alimentos", arrayAlimentos)
            }

            jsonArray.put(novaRefeicao)
            file.writeText(jsonArray.toString(2))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Método auxiliar para buscar todas as refeições salvas
    fun getRefeicoes(): JSONArray {
        val file = getFileRefeicoes()
        val jsonString = file.readText()
        return if (jsonString.isEmpty()) JSONArray("[]") else JSONArray(jsonString)
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
