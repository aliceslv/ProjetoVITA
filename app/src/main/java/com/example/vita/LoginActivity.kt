package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbManager: JsonBD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbManager = JsonBD(this)

        binding.icarrow.setOnClickListener {
            finish()
        }

        binding.createBtn2.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        binding.forgotBtn3.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
        }

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

            // Valida as credenciais comparando com o JSON local
            val loginValido = dbManager.validateLogin(email, senha)

            if (loginValido) {
                Toast.makeText(
                    this,
                    "Login realizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                // Redireciona para a tela principal (ex: MainActivity)
                // val intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish()
            } else {
                Toast.makeText(
                    this,
                    "E-mail ou senha incorretos!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}