package com.example.vita // Altere para o pacote do seu projeto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vita.databinding.ActivityCadastroBinding // Verifique o nome gerado automaticamente

class CadastroActivity : AppCompatActivity() {

    // Instância do ViewBinding
    private lateinit var binding: ActivityCadastroBinding

    // Instâncias do Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla a view usando o Binding
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializando o Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Botão de voltar (Seta)
        binding.icarrow.setOnClickListener {
            finish()
        }

        // Navegação para a tela de Login
        binding.loginBtn3.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        // Botão de Registrar
        binding.createBtn.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()

            // Validação simples
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

            // Inicia o cadastro
            cadastrarUsuario(nome, email, senha)
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        salvarDadosNoFirestore(userId, nome, email)
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
    }
}