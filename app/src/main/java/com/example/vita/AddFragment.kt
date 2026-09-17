package com.example.vita

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    // Métricas base calculadas para 1g
    private var caloriasPorGrama = 0f
    private var carbosPorGrama = 0f
    private var proteinasPorGrama = 0f
    private var gordurasPorGrama = 0f

    // Peso da porção padrão em gramas (ex: 50g)
    private var gramasPorPorcao = 100f

    private var isUpdatingText = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foodName = arguments?.getString("FOOD_NAME") ?: "Alimento"
        val foodDescription = arguments?.getString("FOOD_DESCRIPTION") ?: ""

        exibirInformacoesAlimento(foodName, foodDescription)
        configurarInputsQuantidade()
        configurarNavegacao()
    }

    private fun exibirInformacoesAlimento(nome: String, descricao: String) {
        binding.tvTituloAlimento.text = nome
        binding.tvDescricaoCurta.text = if (descricao.isNotEmpty()) descricao else "Sem descrição disponível."

        // Extrai métricas base por 100g
        val cal100g = extrairValorFloat(descricao, "Calories:", "kcal")
        val carb100g = extrairValorFloat(descricao, "Carbs:", "g")
        val prot100g = extrairValorFloat(descricao, "Protein:", "g")
        val gord100g = extrairValorFloat(descricao, "Fat:", "g")

        // Converte para base de 1 grama
        caloriasPorGrama = cal100g / 100f
        carbosPorGrama = carb100g / 100f
        proteinasPorGrama = prot100g / 100f
        gordurasPorGrama = gord100g / 100f

        // Inicializa com valor padrão de 100g
        binding.etGramas.setText("100")
        atualizarValoresNutricionais(100f)
    }

    private fun configurarInputsQuantidade() {
        // Listener para o campo de GRAMAS
        binding.etGramas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingText) return
                val gramas = s?.toString()?.toFloatOrNull() ?: 0f

                isUpdatingText = true
                // Atualiza o campo de unidades de forma proporcional
                val porcoes = if (gramasPorPorcao > 0) gramas / gramasPorPorcao else 0f
                binding.etUnidades.setText(if (porcoes > 0) String.format("%.1f", porcoes) else "")
                isUpdatingText = false

                atualizarValoresNutricionais(gramas)
            }
        })

        // Listener para o campo de UNIDADES / PORÇÕES
        binding.etUnidades.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingText) return
                val porcoes = s?.toString()?.toFloatOrNull() ?: 0f
                val gramasCorrespondentes = porcoes * gramasPorPorcao

                isUpdatingText = true
                // Atualiza o campo de gramas de forma proporcional
                binding.etGramas.setText(if (gramasCorrespondentes > 0) String.format("%.0f", gramasCorrespondentes) else "")
                isUpdatingText = false

                atualizarValoresNutricionais(gramasCorrespondentes)
            }
        })
    }

    private fun atualizarValoresNutricionais(gramasTotais: Float) {
        val carbosTotais = carbosPorGrama * gramasTotais
        val proteinasTotais = proteinasPorGrama * gramasTotais
        val gordurasTotais = gordurasPorGrama * gramasTotais
        val caloriasTotais = caloriasPorGrama * gramasTotais

        binding.tvCarboidratos.text = String.format("%.1fg", carbosTotais)
        binding.tvProteinas.text = String.format("%.1fg", proteinasTotais)
        binding.tvGorduras.text = String.format("%.1fg", gordurasTotais)
        binding.tvCaloriasPorGrama.text = String.format("%.0f kcal", caloriasTotais)
    }

    private fun extrairValorFloat(texto: String, chaveInicio: String, chaveFim: String): Float {
        return try {
            if (texto.contains(chaveInicio)) {
                val inicio = texto.indexOf(chaveInicio) + chaveInicio.length
                val fim = texto.indexOf(chaveFim, inicio)
                if (fim != -1) {
                    val valorStr = texto.substring(inicio, fim).trim().replace(",", ".")
                    valorStr.toFloatOrNull() ?: 0f
                } else 0f
            } else 0f
        } catch (e: Exception) {
            0f
        }
    }

    private fun configurarNavegacao() {
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAdicionarMais.setOnClickListener {
            Toast.makeText(requireContext(), "Alimento adicionado!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        binding.btnFinalizarRegistro.setOnClickListener {
            Toast.makeText(requireContext(), "Refeição registrada com sucesso!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}