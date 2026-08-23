package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityInformationsBinding

class Informations : AppCompatActivity() {

    private lateinit var binding: ActivityInformationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Resgata os dados enviados pelas telas anteriores (CadastroActivity e GoalsActivity)
        val nome = intent.getStringExtra("NOME")
        val email = intent.getStringExtra("EMAIL")
        val senha = intent.getStringExtra("SENHA")
        val metaSelecionada = intent.getStringExtra("META_SELECIONADA")

        // 1. Configuração do Spinner (Dropdown de Sexo)
        configurarSpinnerSexo()

        // 2. Voltar para a GoalsActivity ao clicar na seta
        binding.icarrow.setOnClickListener {
            finish()
        }

        // 3. Clique do botão Continuar -> WorkoutActivity
        binding.continueBtn.setOnClickListener {
            val peso = binding.edtPeso.text.toString().trim()
            val altura = binding.edtAltura.text.toString().trim()
            val pesoMeta = binding.edtPesoMeta.text.toString().trim()
            val sexo = binding.spinnerSexo.selectedItem.toString()
            val nasc = binding.edtNasc.text.toString().trim()

            // Validação simples dos campos
            if (peso.isEmpty() || altura.isEmpty() || pesoMeta.isEmpty() || nasc.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Repassa TODOS os dados acumulados (Cadastro + Goals + Informations) para a WorkoutActivity
            val intent = Intent(this, WorkoutActivity::class.java).apply {
                putExtra("NOME", nome)
                putExtra("EMAIL", email)
                putExtra("SENHA", senha)
                putExtra("META_SELECIONADA", metaSelecionada)
                putExtra("PESO", peso)
                putExtra("ALTURA", altura)
                putExtra("PESO_META", pesoMeta)
                putExtra("SEXO", sexo)
                putExtra("NASCIMENTO", nasc)
            }
            startActivity(intent)
        }
    }

    private fun configurarSpinnerSexo() {
        val opcoesSexo = arrayOf("Masculino", "Feminino")

        // Utiliza o layout customizado dropdown_sexo.xml
        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_sexo,
            opcoesSexo
        )

        // Define o estilo do menu suspenso
        adapter.setDropDownViewResource(R.layout.dropdown_sexo)

        binding.spinnerSexo.adapter = adapter
    }
}