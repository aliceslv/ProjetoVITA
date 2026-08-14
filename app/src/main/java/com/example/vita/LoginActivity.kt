package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // Declaração da variável do binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout e define como a view principal
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão de voltar (Seta)
        binding.icarrow.setOnClickListener {
            finish() // Fecha a Activity atual e volta para a anterior
        }

        // Navegação para a tela de Cadastro
        binding.createBtn2.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Navegação para a tela de Reset
        binding.forgotBtn3.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
        }

        // Ação do botão Entrar
        binding.loginBtn.setOnClickListener {

            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString()

            if (email.isEmpty()) {
                binding.edtEmail.error = "Digite seu e-mail"
                binding.edtEmail.requestFocus()
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                binding.edtSenha.error = "Digite sua senha"
                binding.edtSenha.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Login realizado com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}