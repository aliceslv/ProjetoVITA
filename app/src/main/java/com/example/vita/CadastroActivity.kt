package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityCadastroBinding

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

            // Redireciona para a GoalsActivity repassando Nome, E-mail e Senha sem salvar no Firebase ainda
            val intent = Intent(this, GoalsActivity::class.java).apply {
                putExtra("NOME", nome)
                putExtra("EMAIL", email)
                putExtra("SENHA", senha)
            }
            startActivity(intent)
        }
    }
}