package com.example.vita

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityGoalsBinding

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão para voltar: fecha a tela atual mantendo o estado da anterior
        binding.icarrow.setOnClickListener {
            finish()
        }

        // Opção 1: Emagrecimento
        binding.btnweightloss.setOnClickListener {
            navegarParaInformations("Emagrecimento")
        }

        // Opção 2: Manter Peso
        binding.btnKW.setOnClickListener {
            navegarParaInformations("Manter peso")
        }

        // Opção 3: Ganho de Massa
        binding.btnGW.setOnClickListener {
            navegarParaInformations("Ganho de massa")
        }
    }

    private fun navegarParaInformations(metaSelecionada: String) {
        // Resgata os dados vindos da CadastroActivity
        val nome = intent.getStringExtra("NOME")
        val email = intent.getStringExtra("EMAIL")
        val senha = intent.getStringExtra("SENHA")

        // Repassa os dados de cadastro + a meta selecionada para a Informations
        val intent = Intent(this, Informations::class.java).apply {
            putExtra("NOME", nome)
            putExtra("EMAIL", email)
            putExtra("SENHA", senha)
            putExtra("META_SELECIONADA", metaSelecionada)
        }
        startActivity(intent)
    }
}