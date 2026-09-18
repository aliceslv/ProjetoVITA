package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.example.vita.data.network.FatSecretClient
import com.example.vita.databinding.FragmentRegisterBinding
import com.example.vita.ui.AlimentoAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RefeicaoViewModel by activityViewModels()

    private lateinit var alimentoAdapter: AlimentoAdapter
    private var tokenAcesso: String? = null
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FatSecret", "RegisterFragment carregado (onViewCreated)")

        atualizarDataAtual()
        configurarEstiloCabecalho()
        configurarRecyclerView()
        configurarBotoes()
        autenticarEBuscarExemplos()
        configurarCampoBusca()
        observarCarrinho()
    }

    private fun observarCarrinho() {
        viewModel.listaAlimentos.observe(viewLifecycleOwner) { lista ->
            if (lista.isNotEmpty()) {
                binding.layoutCarrinhoInfo.visibility = View.VISIBLE
                val totalCalorias = viewModel.obterTotalCalorias()
                binding.tvItensCarrinho.text = "${lista.size} alimento(s) no carrinho (%.0f kcal)".format(totalCalorias)
            } else {
                binding.layoutCarrinhoInfo.visibility = View.GONE
            }
        }

        binding.btnLimparCarrinho.setOnClickListener {
            viewModel.limparCarrinho()
        }
    }

    private fun atualizarDataAtual() {
        val formatoData = SimpleDateFormat("EEEE, MMM. dd", Locale("pt", "BR"))
        binding.dataReal.text = formatoData.format(Date())
    }

    private fun configurarEstiloCabecalho() {
        val corVerde = ContextCompat.getColor(requireContext(), R.color.green2)
        binding.alimentoAba.setTextColor(corVerde)
        binding.consumoAba.setTextColor(Color.WHITE)
    }

    private fun configurarRecyclerView() {
        alimentoAdapter = AlimentoAdapter(emptyList()) { alimentoSelecionado ->
            val bundle = Bundle().apply {
                putString("FOOD_NAME", alimentoSelecionado.foodName)
                putString("FOOD_DESCRIPTION", alimentoSelecionado.foodDescription)
            }

            if (findNavController().currentDestination?.id == R.id.registerFragment) {
                try {
                    findNavController().navigate(
                        R.id.action_registerFragment_to_addFragment,
                        bundle
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
            if (findNavController().currentDestination?.id == R.id.registerFragment) {
                try {
                    findNavController().navigate(R.id.action_registerFragment_to_inicioFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.consumoAba.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.registerFragment) {
                try {
                    findNavController().navigate(R.id.action_registerFragment_to_recentFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun autenticarEBuscarExemplos() {
        lifecycleScope.launch {
            try {
                Log.d("FatSecret", "Solicitando token...")
                val headerAuth = FatSecretClient.getBasicAuthHeader()
                val tokenResponse = FatSecretClient.apiTokenService.getAccessToken(headerAuth)
                tokenAcesso = tokenResponse.accessToken
                Log.d("FatSecret", "Token obtido com sucesso: ${tokenAcesso?.take(10)}...")

                realizarBuscaApi("Frango")
            } catch (e: Exception) {
                Log.e("FatSecret", "Erro ao obter token", e)
                Toast.makeText(context, "Erro ao autenticar com FatSecret", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarCampoBusca() {
        binding.etPesquisa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)
                    if (query.length >= 2) {
                        realizarBuscaApi(query)
                    } else if (query.isEmpty()) {
                        realizarBuscaApi("Frango")
                    }
                }
            }
        })
    }

    private fun realizarBuscaApi(termo: String) {
        val token = tokenAcesso ?: run {
            Log.w("FatSecret", "Busca cancelada: token nulo")
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("FatSecret", "Buscando alimento: $termo")
                val bearerHeader = "Bearer $token"
                val searchResult = FatSecretClient.apiService.buscarAlimentos(
                    bearerToken = bearerHeader,
                    method = "foods.search",
                    query = termo,
                    format = "json"
                )

                if (searchResult.error != null) {
                    Log.e("FatSecret", "Erro da API: ${searchResult.error.code} - ${searchResult.error.message}")
                    Toast.makeText(context, "Erro FatSecret: ${searchResult.error.message}", Toast.LENGTH_LONG).show()
                    alimentoAdapter.atualizarLista(emptyList())
                    return@launch
                }

                val listaResultado = searchResult.foods?.foodList ?: emptyList()
                Log.d("FatSecret", "Resultados encontrados: ${listaResultado.size}")
                alimentoAdapter.atualizarLista(listaResultado)

            } catch (e: Exception) {
                Log.e("FatSecret", "Erro ao buscar alimento", e)
                alimentoAdapter.atualizarLista(emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}