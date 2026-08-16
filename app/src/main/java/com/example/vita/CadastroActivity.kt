package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dbManager: JsonBD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dbManager = JsonBD(this)

        binding.icarrow.setOnClickListener {
            finish()
        }

        binding.loginBtn3.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.createBtn.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.length < 6) {
                Toast.makeText(
                    this,
                    "A senha deve ter pelo menos 6 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            cadastrarUsuario(nome, email, senha)
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: System.currentTimeMillis().toString()

                    // 1. Salva no Firestore
                    salvarDadosNoFirestore(userId, nome, email)

                    // 2. Salva no JSON local
                    val salvouJson = dbManager.addUser(userId, nome, email, senha)

                    if (salvouJson) {
                        Toast.makeText(this, "Cadastro realizado e salvo no JSON!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "E-mail já consta no arquivo JSON!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val erro = task.exception?.message ?: "Erro ao cadastrar usuário."
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun salvarDadosNoFirestore(userId: String, nome: String, email: String) {
        val usuarioMap = hashMapOf(
            "nome" to nome,
            "email" to email
        )

        db.collection("usuarios")
            .document(userId)
            .set(usuarioMap)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro no Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}