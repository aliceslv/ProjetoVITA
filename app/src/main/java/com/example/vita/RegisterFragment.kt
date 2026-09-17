package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

        // 1. Atualiza a data com o dia atual
        atualizarDataAtual()

        // 2. Destaca a aba ativa (ALIMENTO = Verde, CONSUMIDO = Branco)
        configurarEstiloCabecalho()

        // 3. Inicializa lista, botões e eventos de busca
        configurarRecyclerView()
        configurarBotoes()
        autenticarEBuscarExemplos()
        configurarCampoBusca()
    }

    private fun atualizarDataAtual() {
        val formatoData = SimpleDateFormat("EEEE, MMM. dd", Locale("pt", "BR"))
        val dataFormatada = formatoData.format(Date())
        binding.dataReal.text = dataFormatada
    }

    private fun configurarEstiloCabecalho() {
        val corVerde = ContextCompat.getColor(requireContext(), R.color.green2)
        val corBranca = Color.WHITE

        // Como esta é a tela principal de Registro/Alimentos:
        // ALIMENTO -> VERDE
        // CONSUMIDO RECENTEMENTE -> BRANCO
        binding.alimentoAba.setTextColor(corVerde)
        binding.consumoAba.setTextColor(corBranca)
    }

    private fun configurarRecyclerView() {
        alimentoAdapter = AlimentoAdapter(emptyList()) { alimentoSelecionado ->
            val bundle = Bundle().apply {
                putString("FOOD_NAME", alimentoSelecionado.foodName)
                putString("FOOD_DESCRIPTION", alimentoSelecionado.foodDescription)
            }

            // Verifica se o fragment atual ainda é o destino visível antes de navegar
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
            findNavController().navigateUp()
        }

        binding.consumoAba.setOnClickListener {
            // Validação de segurança para troca de aba
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
                val headerAuth = FatSecretClient.getBasicAuthHeader()
                val tokenResponse = FatSecretClient.apiTokenService.getAccessToken(headerAuth)
                tokenAcesso = tokenResponse.accessToken

                realizarBuscaApi("Frango")
            } catch (e: Exception) {
                e.printStackTrace()
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
        val token = tokenAcesso ?: return

        lifecycleScope.launch {
            try {
                val bearerHeader = "Bearer $token"
                val searchResult = FatSecretClient.apiService.buscarAlimentos(
                    bearerToken = bearerHeader,
                    query = termo
                )

                val listaResultado = searchResult.foods?.foodList ?: emptyList()
                alimentoAdapter.atualizarLista(listaResultado)

            } catch (e: Exception) {
                e.printStackTrace()
                alimentoAdapter.atualizarLista(emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}