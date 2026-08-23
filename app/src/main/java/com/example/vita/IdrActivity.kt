package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityIdrBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class IdrActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdrBinding
    private lateinit var dbManager: JsonBD
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbManager = JsonBD(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Resgata TODOS os dados acumulados enviados pelas telas anteriores
        val nome = intent.getStringExtra("NOME") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val senha = intent.getStringExtra("SENHA") ?: ""
        val meta = intent.getStringExtra("META_SELECIONADA") ?: "Manter peso"
        val pesoStr = intent.getStringExtra("PESO") ?: "70"
        val alturaStr = intent.getStringExtra("ALTURA") ?: "170"
        val pesoMetaStr = intent.getStringExtra("PESO_META") ?: "70"
        val sexo = intent.getStringExtra("SEXO") ?: "Masculino"
        val nascStr = intent.getStringExtra("NASCIMENTO") ?: "01/01/2000"
        val nivelExercicio = intent.getStringExtra("NIVEL_EXERCICIO") ?: "Baixo"

        // Executa o cálculo da Ingestão Diária Recomendada (IDR)
        val idrFinal = calcularIDR(meta, pesoStr, alturaStr, sexo, nascStr, nivelExercicio)

        // Exibe o resultado na interface
        binding.txtValorCalorias.text = idrFinal.toString()

        // Botão para voltar
        binding.icarrow.setOnClickListener {
            finish()
        }

        // Clique do botão Iniciar Perfil -> Cria no Firebase Auth, Firestore e JSON
        binding.idrbtn.setOnClickListener {
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Dados de cadastro inválidos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cadastrarEGravarDados(
                nome = nome,
                email = email,
                senha = senha,
                meta = meta,
                peso = pesoStr,
                altura = alturaStr,
                pesoMeta = pesoMetaStr,
                sexo = sexo,
                nascimento = nascStr,
                nivelExercicio = nivelExercicio,
                idr = idrFinal
            )
        }
    }

    private fun cadastrarEGravarDados(
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
    ) {
        // 1. Cria o usuário no Firebase Auth
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: System.currentTimeMillis().toString()

                    // 2. Prepara o mapa com TODOS os dados para o Firestore
                    val usuarioMap = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "meta" to meta,
                        "peso" to peso,
                        "altura" to altura,
                        "pesoMeta" to pesoMeta,
                        "sexo" to sexo,
                        "nascimento" to nascimento,
                        "nivelExercicio" to nivelExercicio,
                        "idr" to idr
                    )

                    // 3. Salva no Firestore
                    db.collection("usuarios")
                        .document(userId)
                        .set(usuarioMap)

                    // 4. Salva também no arquivo JSON local
                    dbManager.salvarPerfilUsuario(
                        nome = nome,
                        email = email,
                        senha = senha,
                        meta = meta,
                        peso = peso,
                        altura = altura,
                        pesoMeta = pesoMeta,
                        sexo = sexo,
                        nascimento = nascimento,
                        nivelExercicio = nivelExercicio,
                        idr = idr
                    )

                    Toast.makeText(this, "Perfil cadastrado e salvo com sucesso!", Toast.LENGTH_SHORT).show()

                    // Redireciona para a tela principal (ex: MainActivity)
                    // val intent = Intent(this, MainActivity::class.java)
                    // startActivity(intent)
                    // finish()
                } else {
                    val erro = task.exception?.message ?: "Erro ao cadastrar usuário no Firebase."
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun calcularIDR(
        meta: String,
        pesoStr: String,
        alturaStr: String,
        sexo: String,
        nascStr: String,
        nivelExercicio: String
    ): Int {
        val peso = pesoStr.toDoubleOrNull() ?: 70.0
        val altura = alturaStr.toDoubleOrNull() ?: 170.0
        val idade = calcularIdade(nascStr)

        val tmb = if (sexo.equals("Masculino", ignoreCase = true)) {
            (10 * peso) + (6.25 * altura) - (5 * idade) + 5
        } else {
            (10 * peso) + (6.25 * altura) - (5 * idade) - 161
        }

        val fatorAtividade = when (nivelExercicio) {
            "Baixo" -> 1.2
            "Médio" -> 1.375
            "Alto" -> 1.55
            "Muito Alto" -> 1.725
            else -> 1.2
        }

        val gastoCaloricoTotal = tmb * fatorAtividade

        val idrFinal = when (meta) {
            "Emagrecimento" -> gastoCaloricoTotal - 400
            "Ganho de massa" -> gastoCaloricoTotal + 400
            else -> gastoCaloricoTotal
        }

        return idrFinal.toInt()
    }

    private fun calcularIdade(dataNascimento: String): Int {
        val partes = dataNascimento.split("/")
        if (partes.size != 3) return 25

        val dia = partes[0].toIntOrNull() ?: 1
        val mes = partes[1].toIntOrNull() ?: 1
        val ano = partes[2].toIntOrNull() ?: 2000

        val hoje = Calendar.getInstance()
        var idade = hoje.get(Calendar.YEAR) - ano

        if (hoje.get(Calendar.MONTH) + 1 < mes ||
            (hoje.get(Calendar.MONTH) + 1 == mes && hoje.get(Calendar.DAY_OF_MONTH) < dia)
        ) {
            idade--
        }

        return if (idade < 0) 25 else idade
    }
}