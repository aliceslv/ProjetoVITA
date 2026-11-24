package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.CancellationException
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vita.data.network.FatSecretClient
import com.example.vita.data.network.FoodItem
import com.example.vita.ui.RefeicaoViewModel
import com.example.vita.data.repository.CustomFoodRepository
import com.example.vita.databinding.FragmentRegisterBinding
import com.example.vita.ui.AlimentoAdapter
import com.example.vita.ui.InputUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private val binding: FragmentRegisterBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val viewModel: RefeicaoViewModel by activityViewModels()
    private val customFoodRepository = CustomFoodRepository()
    private var alimentosApiAtuais: List<FoodItem> = emptyList()
    private var categoriaSelecionada = "TODOS"
    private var termoBuscaAtual = ""
    private var alimentosPersonalizados: List<FoodItem> = emptyList()
    private lateinit var alimentoAdapter: AlimentoAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        InputUtils.configurarTeclado(view)

        Log.d("FatSecret", "RegisterFragment carregado")

        atualizarDataAtual()
        configurarEstiloCabecalho()
        configurarRecyclerView()
        configurarBotoes()
        configurarCampoBusca()
        configurarAlimentoPersonalizado()
        observarCarrinho()

        configurarFiltros()

        carregarAlimentosPersonalizados()

        viewLifecycleOwner.lifecycleScope.launch {
            carregarSugestoesCategoria("TODOS")
        }
    }

    override fun onResume() {
        super.onResume()

        carregarAlimentosPersonalizados()
    }

    private fun observarCarrinho() {

        viewModel.listaAlimentos.observe(viewLifecycleOwner) { lista ->

            if (lista.isNotEmpty()) {

                binding.layoutCarrinhoInfo.visibility = View.VISIBLE

                val totalCalorias = viewModel.obterTotalCalorias()

                binding.tvItensCarrinho.text =
                    "${lista.size} alimento(s) adicionado (%.0f kcal)"
                        .format(totalCalorias)

            } else {

                binding.layoutCarrinhoInfo.visibility = View.GONE
            }
        }

        binding.btnLimparCarrinho.setOnClickListener {
            viewModel.limparCarrinho()
        }
    }

    //Lei de hick aplicada
    private fun configurarFiltros() {

        binding.filtroTodos.setOnClickListener {
            selecionarCategoria("TODOS")
        }

        binding.filtroProteinas.setOnClickListener {
            selecionarCategoria("PROTEINAS")
        }

        binding.filtroCarboidratos.setOnClickListener {
            selecionarCategoria("CARBOIDRATOS")
        }

        binding.filtroLanches.setOnClickListener {
            selecionarCategoria("LANCHES")
        }
    }

    private fun selecionarCategoria(categoria: String) {

        categoriaSelecionada = categoria

        val botoes = listOf(
            binding.filtroTodos,
            binding.filtroProteinas,
            binding.filtroCarboidratos,
            binding.filtroLanches
        )

        botoes.forEach { botao ->

            val selecionado = when (botao.id) {
                binding.filtroTodos.id -> categoria == "TODOS"
                binding.filtroProteinas.id -> categoria == "PROTEINAS"
                binding.filtroCarboidratos.id -> categoria == "CARBOIDRATOS"
                binding.filtroLanches.id -> categoria == "LANCHES"
                else -> false
            }

            botao.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (selecionado) R.color.green2 else R.color.blue4
            )

            botao.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (selecionado) R.color.white else R.color.grey2
                )
            )
        }

        if (termoBuscaAtual.isEmpty()) {

            searchJob?.cancel()

            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                carregarSugestoesCategoria(categoria)
            }

        } else {
            atualizarListaAlimentos()
        }
    }

    private fun atualizarListaAlimentos() {

        val todosAlimentos = alimentosPersonalizados + alimentosApiAtuais

        val resultados = todosAlimentos
            .distinctBy { it.foodId }
            .filter { alimento ->

                val correspondeBusca =
                    alimento.foodName.contains(
                        termoBuscaAtual,
                        ignoreCase = true
                    )

                val descricao = alimento.foodDescription.orEmpty()

                val proteinas = extrairNutriente(
                    descricao,
                    listOf("Protein", "Proteína", "Proteinas")
                )

                val carboidratos = extrairNutriente(
                    descricao,
                    listOf("Carbs", "Carbohydrates", "Carboidratos")
                )

                val correspondeCategoria = when (categoriaSelecionada) {

                    "PROTEINAS" -> proteinas >= 5.0

                    "CARBOIDRATOS" -> carboidratos >= 5.0

                    "LANCHES" -> {

                        val nome = alimento.foodName.lowercase(Locale.ROOT)

                        listOf(
                            "lanche",
                            "sanduiche",
                            "sanduíche",
                            "biscoito",
                            "barra",
                            "snack",
                            "chips",
                            "cookie",
                            "iogurte",
                            "yogurt",
                            "granola"
                        ).any { nome.contains(it) }
                    }

                    else -> true
                }

                correspondeBusca && correspondeCategoria
            }

        alimentoAdapter.atualizarLista(resultados)
    }

    private suspend fun carregarSugestoesCategoria(categoria: String) {

        val termos = when (categoria) {

            "PROTEINAS" -> listOf(
                "frango",
                "ovo",
                "atum",
                "carne"
            )

            "CARBOIDRATOS" -> listOf(
                "arroz",
                "macarrão",
                "batata",
                "aveia"
            )

            "LANCHES" -> listOf(
                "sanduíche",
                "iogurte",
                "granola",
                "biscoito"
            )

            else -> listOf(
                "frango",
                "arroz",
                "ovo",
                "macarrão",
                "atum",
                "batata",
                "iogurte",
                "sanduíche"
            )
        }

        try {

            val resultados = coroutineScope {

                termos.map { termo ->

                    async {

                        try {

                            val resposta = FatSecretClient.apiService
                                .buscarAlimentos(query = termo)

                            resposta.foods?.foodList ?: emptyList()

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {

                            Log.e(
                                "FatSecret",
                                "Erro ao buscar sugestões para $termo",
                                e
                            )

                            emptyList()
                        }
                    }

                }.awaitAll().flatten()
            }

            if (_binding == null) return

            alimentosApiAtuais = resultados.distinctBy { it.foodId }

            atualizarListaAlimentos()

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "FatSecret",
                "Erro ao carregar sugestões",
                e
            )
        }
    }

    private fun extrairNutriente(
        descricao: String,
        nomes: List<String>
    ): Double {

        for (nome in nomes) {

            val regex = Regex(
                """${Regex.escape(nome)}:\s*([\d.,]+)\s*g""",
                RegexOption.IGNORE_CASE
            )

            val resultado = regex.find(descricao)

            if (resultado != null) {

                return resultado.groupValues[1]
                    .replace(",", ".")
                    .toDoubleOrNull() ?: 0.0
            }
        }

        return 0.0
    }


    private fun carregarAlimentosPersonalizados() {

        customFoodRepository.buscarAlimentos(

            sucesso = { alimentos: List<Map<String, Any>> ->

                if (!isAdded || _binding == null) return@buscarAlimentos

                alimentosPersonalizados = alimentos.map { alimento ->

                    val nome = alimento["nome"]?.toString() ?: ""

                    val calorias =
                        (alimento["calorias"] as? Number)?.toDouble() ?: 0.0

                    val proteinas =
                        (alimento["proteinas"] as? Number)?.toDouble() ?: 0.0

                    val carboidratos =
                        (alimento["carboidratos"] as? Number)?.toDouble() ?: 0.0

                    val gorduras =
                        (alimento["gorduras"] as? Number)?.toDouble() ?: 0.0

                    val descricao = """
                        Serving Size: 100 g
                        Calories: $calorias kcal
                        Carbs: $carboidratos g
                        Protein: $proteinas g
                        Fat: $gorduras g
                    """.trimIndent()

                    FoodItem(
                        foodId = "custom_${alimento["id"]}",
                        foodName = nome,
                        foodDescription = descricao,
                        foodType = "generic"
                    )
                }

                atualizarListaAlimentos()
            },

            erro = { erro ->

                Log.e(
                    "CustomFood",
                    "Erro ao carregar alimentos personalizados",
                    erro
                )
            }
        )
    }

    private fun atualizarDataAtual() {

        val formatoData = SimpleDateFormat(
            "EEEE, MMM. dd",
            Locale("pt", "BR")
        )

        binding.dataReal.text = formatoData.format(Date())
    }

    private fun configurarEstiloCabecalho() {

        val corVerde = ContextCompat.getColor(
            requireContext(),
            R.color.green2
        )

        binding.alimentoAba.setTextColor(corVerde)
        binding.consumoAba.setTextColor(Color.WHITE)
    }

    private fun configurarRecyclerView() {

        alimentoAdapter = AlimentoAdapter(emptyList()) { alimentoSelecionado ->

            val bundle = Bundle().apply {

                putString("FOOD_NAME", alimentoSelecionado.foodName)

                putString(
                    "FOOD_DESCRIPTION",
                    alimentoSelecionado.foodDescription
                )

                putString(
                    "FOOD_ORIGIN",
                    if (alimentoSelecionado.foodId.startsWith("custom_")) {
                        "personalizado"
                    } else {
                        "fatsecret"
                    }
                )
            }

            if (
                findNavController().currentDestination?.id ==
                R.id.registerFragment
            ) {

                findNavController().navigate(
                    R.id.action_registerFragment_to_addFragment,
                    bundle
                )
            }
        }

        binding.rvAlimentos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alimentoAdapter
        }
    }

    private fun configurarBotoes() {
        binding.btnCancelar.setOnClickListener {
            viewModel.limparCarrinho()

            if (
                findNavController().currentDestination?.id ==
                R.id.registerFragment
            ) {

                findNavController().navigate(
                    R.id.action_registerFragment_to_inicioFragment
                )
            }
        }

        binding.consumoAba.setOnClickListener {
            if (
                findNavController().currentDestination?.id ==
                R.id.registerFragment
            ) {
                findNavController().navigate(
                    R.id.action_registerFragment_to_recentFragment
                )
            }
        }
    }

    private fun configurarCampoBusca() {
        binding.etPesquisa.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                termoBuscaAtual = query

                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)

                    if (query.length >= 2) {
                        realizarBuscaApi(query)

                    } else if (query.isEmpty()) {
                        carregarSugestoesCategoria(categoriaSelecionada)
                    }
                }
            }
        })
    }
    private fun configurarAlimentoPersonalizado() {
        binding.btnCustomFood.setOnClickListener {
            if (
                findNavController().currentDestination?.id ==
                R.id.registerFragment
            ) {
                findNavController().navigate(
                    R.id.action_registerFragment_to_customFoodFragment
                )
            }
        }
    }

    // Lei de hick aplicada
    //suspend aplicado com coroutine para a interface não travar na busca
    private suspend fun realizarBuscaApi(termo: String) {

        try {

            val searchResult = FatSecretClient.apiService.buscarAlimentos(
                query = termo
            )

            if (_binding == null) return

            if (searchResult.error != null) {

                Log.e(
                    "FatSecret",
                    "Erro do Proxy: ${searchResult.error.message}"
                )

                alimentosApiAtuais = emptyList()
                atualizarListaAlimentos()

                return
            }

            alimentosApiAtuais =
                searchResult.foods?.foodList ?: emptyList()

            atualizarListaAlimentos()

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "FatSecret",
                "Erro ao buscar alimentos",
                e
            )

            if (_binding == null) return

            alimentosApiAtuais = emptyList()
            atualizarListaAlimentos()
        }
    }


    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
