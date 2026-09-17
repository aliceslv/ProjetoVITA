package com.example.vita

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.data.model.AlimentoConsumido
import com.example.vita.databinding.FragmentAddBinding
import com.example.vita.json.JsonBD
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartilhada na Activity para manter o carrinho entre Fragments
    private val viewModel: RefeicaoViewModel by activityViewModels()

    private var caloriasPorGrama = 0f
    private var carbosPorGrama = 0f
    private var proteinasPorGrama = 0f
    private var gordurasPorGrama = 0f
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

        val cal100g = extrairValorFloat(descricao, listOf("Calories:", "Calorias:"))
        val carb100g = extrairValorFloat(descricao, listOf("Carbs:", "Carboidratos:", "Carb:"))
        val prot100g = extrairValorFloat(descricao, listOf("Protein:", "Proteínas:", "Proteina:"))
        val gord100g = extrairValorFloat(descricao, listOf("Fat:", "Gorduras:", "Gordura:"))

        caloriasPorGrama = cal100g / 100f
        carbosPorGrama = carb100g / 100f
        proteinasPorGrama = prot100g / 100f
        gordurasPorGrama = gord100g / 100f

        binding.etGramas.setText("100")
        atualizarValoresNutricionais(100f)
    }

    private fun configurarInputsQuantidade() {
        binding.etGramas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingText) return
                val gramas = s?.toString()?.replace(",", ".")?.toFloatOrNull() ?: 0f

                isUpdatingText = true
                val porcoes = if (gramasPorPorcao > 0) gramas / gramasPorPorcao else 0f
                binding.etUnidades.setText(if (porcoes > 0) String.format(Locale.US, "%.1f", porcoes) else "")
                isUpdatingText = false

                atualizarValoresNutricionais(gramas)
            }
        })

        binding.etUnidades.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingText) return
                val porcoes = s?.toString()?.replace(",", ".")?.toFloatOrNull() ?: 0f
                val gramasCorrespondentes = porcoes * gramasPorPorcao

                isUpdatingText = true
                binding.etGramas.setText(if (gramasCorrespondentes > 0) String.format(Locale.US, "%.0f", gramasCorrespondentes) else "")
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

        binding.tvCarboidratos.text = String.format(Locale.US, "%.1fg", carbosTotais)
        binding.tvProteinas.text = String.format(Locale.US, "%.1fg", proteinasTotais)
        binding.tvGorduras.text = String.format(Locale.US, "%.1fg", gordurasTotais)
        binding.tvCaloriasPorGrama.text = String.format(Locale.US, "%.0f kcal", caloriasTotais)
    }

    private fun extrairValorFloat(texto: String, chaves: List<String>): Float {
        for (chave in chaves) {
            if (texto.contains(chave, ignoreCase = true)) {
                try {
                    val sub = texto.substring(texto.indexOf(chave, ignoreCase = true) + chave.length)
                    val regex = Regex("""\d+([.,]\d+)?""")
                    val match = regex.find(sub)
                    if (match != null) {
                        return match.value.replace(",", ".").toFloatOrNull() ?: 0f
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return 0f
    }

    private fun criarAlimentoAtual(): AlimentoConsumido {
        val gramas = binding.etGramas.text.toString().replace(",", ".").toFloatOrNull() ?: 0f
        val porcoes = binding.etUnidades.text.toString().replace(",", ".").toFloatOrNull() ?: 0f

        return AlimentoConsumido(
            nome = binding.tvTituloAlimento.text.toString(),
            gramas = gramas,
            porcoes = porcoes,
            calorias = caloriasPorGrama * gramas,
            carboidratos = carbosPorGrama * gramas,
            proteinas = proteinasPorGrama * gramas,
            gorduras = gordurasPorGrama * gramas
        )
    }

    private fun configurarNavegacao() {
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancelar.setOnClickListener {
            viewModel.limparCarrinho()
            if (findNavController().currentDestination?.id == R.id.addFragment) {
                findNavController().navigate(R.id.action_addFragment_to_inicioFragment)
            }
        }

        // ADICIONAR MAIS: Salva o alimento no carrinho e volta para o RegisterFragment
        binding.btnAdicionarMais.setOnClickListener {
            val alimento = criarAlimentoAtual()
            viewModel.adicionarAlimento(alimento)

            Toast.makeText(requireContext(), "${alimento.nome} adicionado ao carrinho!", Toast.LENGTH_SHORT).show()

            if (findNavController().currentDestination?.id == R.id.addFragment) {
                findNavController().navigate(R.id.action_addFragment_to_registerFragment)
            }
        }

        // FINALIZAR REGISTRO: Adiciona o item atual, salva toda a lista no JSON e limpa o carrinho
        binding.btnFinalizarRegistro.setOnClickListener {
            val alimento = criarAlimentoAtual()
            viewModel.adicionarAlimento(alimento)

            val todosAlimentos = viewModel.listaAlimentos.value ?: emptyList()
            val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val jsonBD = JsonBD(requireContext())
            val sucesso = jsonBD.salvarRefeicao(
                emailUsuario = "usuario@email.com", // Substituir pelo email logado
                tipoRefeicao = "Almoço",
                data = dataHoje,
                alimentos = todosAlimentos
            )

            if (sucesso) {
                Toast.makeText(requireContext(), "Refeição registrada com sucesso com ${todosAlimentos.size} alimento(s)!", Toast.LENGTH_LONG).show()
                viewModel.limparCarrinho()
                findNavController().navigate(R.id.action_addFragment_to_inicioFragment)
            } else {
                Toast.makeText(requireContext(), "Erro ao salvar refeição.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}