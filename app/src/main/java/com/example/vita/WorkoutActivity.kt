package com.example.vita

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityWorkoutBinding

class WorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão para voltar: fecha a tela atual mantendo o estado da anterior
        binding.icarrow.setOnClickListener {
            finish()
        }

        // Configuração dos 4 botões de nível de exercício
        binding.btnBaixo.setOnClickListener {
            navegarParaIdr("Baixo")
        }

        binding.btnMedio.setOnClickListener {
            navegarParaIdr("Médio")
        }

        binding.btnAlto.setOnClickListener {
            navegarParaIdr("Alto")
        }

        binding.btnMuitoAlto.setOnClickListener {
            navegarParaIdr("Muito Alto")
        }
    }

    private fun navegarParaIdr(nivelExercicio: String) {
        // Resgata TODOS os dados acumulados enviados pela Informations (Cadastro + Goals + Informations)
        val nome = intent.getStringExtra("NOME")
        val email = intent.getStringExtra("EMAIL")
        val senha = intent.getStringExtra("SENHA")
        val meta = intent.getStringExtra("META_SELECIONADA")
        val peso = intent.getStringExtra("PESO")
        val altura = intent.getStringExtra("ALTURA")
        val pesoMeta = intent.getStringExtra("PESO_META")
        val sexo = intent.getStringExtra("SEXO")
        val nasc = intent.getStringExtra("NASCIMENTO")

        // Envia TODAS as informações acumuladas + o nível de exercício para a IdrActivity
        val intent = Intent(this, IdrActivity::class.java).apply {
            putExtra("NOME", nome)
            putExtra("EMAIL", email)
            putExtra("SENHA", senha)
            putExtra("META_SELECIONADA", meta)
            putExtra("PESO", peso)
            putExtra("ALTURA", altura)
            putExtra("PESO_META", pesoMeta)
            putExtra("SEXO", sexo)
            putExtra("NASCIMENTO", nasc)
            putExtra("NIVEL_EXERCICIO", nivelExercicio)
        }
        startActivity(intent)
    }
}