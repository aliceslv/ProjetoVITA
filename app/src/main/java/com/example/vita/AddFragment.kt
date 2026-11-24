package com.example.vita

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.vita.ui.InputUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.ui.AlimentoConsumido
import com.example.vita.ui.RefeicaoViewModel
import com.example.vita.data.FirestoreData
import com.example.vita.data.repository.RefeicaoRepository
import com.example.vita.databinding.FragmentAddBinding
import com.example.vita.ui.UnidadeConverter
import java.util.Locale

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null

    private val binding: FragmentAddBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    // ViewModel compartilhada na Activity para manter o carrinho entre Fragments
    private val viewModel: RefeicaoViewModel by activityViewModels()

    // Macronutrientes por grama
    private var caloriasPorGrama = 0f
    private var carbosPorGrama = 0f
    private var proteinasPorGrama = 0f
    private var gordurasPorGrama = 0f

    // Micronutrientes por grama
    private var fibrasPorGrama = 0f
    private var acucarPorGrama = 0f
    private var sodioPorGrama = 0f
    private var colesterolPorGrama = 0f
    private var gorduraSaturadaPorGrama = 0f

    private var gramasPorPorcao = 100f

    private var origemAlimento = "desconhecida"
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

        InputUtils.configurarTeclado(view)

        val foodName = arguments?.getString("FOOD_NAME") ?: "Alimento"
        val foodDescription = arguments?.getString("FOOD_DESCRIPTION") ?: ""
        val foodOrigin = arguments?.getString("FOOD_ORIGIN")
            ?: "desconhecida"

        origemAlimento = foodOrigin

        exibirInformacoesAlimento(foodName, foodDescription)
        configurarInputsQuantidade()
        configurarNavegacao()
    }

    private fun exibirInformacoesAlimento(nome: String, descricao: String) {
        binding.tvTituloAlimento.text = nome
        binding.tvDescricaoCurta.text = if (descricao.isNotEmpty()) descricao else "Sem descrição disponível."

        // 1. Descobre a porção em gramas/ml usando a classe utilitária UnidadeConverter
        val quantidadeEmGramas = UnidadeConverter.extrairGramasDaDescricao(descricao, 100f)
        gramasPorPorcao = if (quantidadeEmGramas > 0f) quantidadeEmGramas else 100f

        // 2. Extrai os valores totais contidos nessa porção (suporta PT e EN)
        val calTotais = extrairValorRegex(descricao, listOf("Calories", "Calorias"), "k?cal")
        val carbTotais = extrairValorRegex(descricao, listOf("Carbs", "Carboidratos"), "g")
        val protTotais = extrairValorRegex(descricao, listOf("Protein", "Proteína", "Proteina", "Prot."), "g")
        val gordTotais = extrairValorRegex(descricao, listOf("Fat", "Gordura", "Gord."), "g")

        // 3. Extrai micronutrientes
        val fibrasTotais = extrairValorRegex(descricao, listOf("Fiber", "Fibras", "Fibra"), "g")
        val acucarTotais = extrairValorRegex(descricao, listOf("Sugar", "Açúcar", "Acucar"), "g")
        val sodioTotais = extrairValorRegex(descricao, listOf("Sodium", "Sódio", "Sodio"), "mg")
        val colesterolTotais = extrairValorRegex(descricao, listOf("Cholesterol", "Colesterol"), "mg")
        val gordSaturadaTotais = extrairValorRegex(descricao, listOf("Saturated Fat", "Gordura Saturada"), "g")

        // 4. Calcula o valor exato por 1 g/ml
        caloriasPorGrama = calTotais / gramasPorPorcao
        carbosPorGrama = carbTotais / gramasPorPorcao
        proteinasPorGrama = protTotais / gramasPorPorcao
        gordurasPorGrama = gordTotais / gramasPorPorcao

        fibrasPorGrama = fibrasTotais / gramasPorPorcao
        acucarPorGrama = acucarTotais / gramasPorPorcao
        sodioPorGrama = sodioTotais / gramasPorPorcao
        colesterolPorGrama = colesterolTotais / gramasPorPorcao
        gorduraSaturadaPorGrama = gordSaturadaTotais / gramasPorPorcao

        // Inicializa o campo com a quantidade da porção padrão
        val gramasIniciais = String.format(Locale.US, "%.0f", gramasPorPorcao)
        binding.etGramas.setText(gramasIniciais)
        atualizarValoresNutricionais(gramasPorPorcao)
    }

    private fun extrairValorRegex(texto: String, chaves: List<String>, sufixo: String): Float {
        for (chave in chaves) {
            val padrao = """$chave:\s*(\d+(?:[.,]\d+)?)\s*$sufixo"""
            val regex = Regex(padrao, RegexOption.IGNORE_CASE)
            val match = regex.find(texto)
            if (match != null) {
                return match.groupValues[1].replace(",", ".").toFloatOrNull() ?: 0f
            }
        }
        return 0f
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
                binding.etUnidades.setText(
                    if (porcoes > 0) String.format(Locale.US, "%.1f", porcoes) else ""
                )
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
                binding.etGramas.setText(
                    if (gramasCorrespondentes > 0) String.format(Locale.US, "%.0f", gramasCorrespondentes) else ""
                )
                isUpdatingText = false

                atualizarValoresNutricionais(gramasCorrespondentes)
            }
        })
    }

    private fun atualizarValoresNutricionais(gramasTotais: Float) {
        val gramasValidas = gramasTotais.coerceIn(0f, 5000f)

        val carbosTotais = carbosPorGrama * gramasValidas
        val proteinasTotais = proteinasPorGrama * gramasValidas
        val gordurasTotais = gordurasPorGrama * gramasValidas
        val caloriasTotais = caloriasPorGrama * gramasValidas

        binding.tvCarboidratos.text = String.format(Locale.US, "%.1fg", carbosTotais)
        binding.tvProteinas.text = String.format(Locale.US, "%.1fg", proteinasTotais)
        binding.tvGorduras.text = String.format(Locale.US, "%.1fg", gordurasTotais)
        binding.tvCaloriasPorGrama.text = String.format(Locale.US, "%.0f kcal", caloriasTotais)
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
            gorduras = gordurasPorGrama * gramas,
            origem = origemAlimento
        )
    }

    private fun configurarNavegacao() {

        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancelar.setOnClickListener {
            cancelarRegistro()
        }

        binding.btnAdicionarMais.setOnClickListener {
            adicionarMaisAlimentos()
        }

        binding.btnFinalizarRegistro.setOnClickListener {
            finalizarRegistro()
        }
    }

    private fun cancelarRegistro() {

        viewModel.limparCarrinho()

        if (findNavController().currentDestination?.id == R.id.addFragment) {
            findNavController().navigate(
                R.id.action_addFragment_to_inicioFragment
            )
        }
    }

    private fun adicionarMaisAlimentos() {

        val alimento = criarAlimentoAtual()

        viewModel.adicionarAlimento(alimento)

        Toast.makeText(
            requireContext(),
            "${alimento.nome} adicionado ao carrinho!",
            Toast.LENGTH_SHORT
        ).show()

        if (findNavController().currentDestination?.id == R.id.addFragment) {
            findNavController().navigate(
                R.id.action_addFragment_to_registerFragment
            )
        }
    }

    private fun finalizarRegistro() {

        val alimento = criarAlimentoAtual()

        viewModel.adicionarAlimento(alimento)

        val todosAlimentos =
            viewModel.listaAlimentos.value ?: emptyList()

        val uid = FirestoreData.uid()

        if (uid == null) {
            Toast.makeText(
                requireContext(),
                "Erro: Usuário não autenticado.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (todosAlimentos.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Adicione pelo menos um alimento.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        salvarRefeicao(uid, todosAlimentos)
    }

    private fun salvarRefeicao(
        uid: String,
        alimentos: List<AlimentoConsumido>
    ) {

        binding.btnFinalizarRegistro.isEnabled = false

        RefeicaoRepository().salvarRefeicao(
            uid = uid,
            tipo = "Almoço",
            alimentos = alimentos,

            sucesso = {

                if (!isAdded) return@salvarRefeicao

                binding.btnFinalizarRegistro.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Refeição registrada com sucesso com ${alimentos.size} alimento(s)!",
                    Toast.LENGTH_LONG
                ).show()

                viewModel.limparCarrinho()

                findNavController().navigate(
                    R.id.action_addFragment_to_inicioFragment
                )
            },

            erro = { erro ->

                if (!isAdded) return@salvarRefeicao

                binding.btnFinalizarRegistro.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar refeição: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}