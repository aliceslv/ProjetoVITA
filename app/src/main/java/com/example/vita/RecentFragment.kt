package com.example.vita

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vita.data.network.FoodItem
import com.example.vita.databinding.FragmentRecentBinding
import com.example.vita.json.JsonBD
import com.example.vita.ui.AlimentoAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFragment : Fragment() {

    private var _binding: FragmentRecentBinding? = null
    private val binding get() = _binding!!

    private lateinit var alimentoAdapter: AlimentoAdapter
    private var listaAlimentosRecentes: List<FoodItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        atualizarDataAtual()
        configurarEstiloCabecalho()
        configurarRecyclerView()
        carregarAlimentosRecentes()
        configurarNavegacao()
        configurarBuscaLocal()
    }

    private fun atualizarDataAtual() {
        val formatoData = SimpleDateFormat("EEEE, MMM. dd", Locale("pt", "BR"))
        binding.dataReal.text = formatoData.format(Date())
    }

    private fun configurarEstiloCabecalho() {
        val corVerde = ContextCompat.getColor(requireContext(), R.color.green2)
        binding.consumoAba.setTextColor(corVerde)
        binding.alimentoAba.setTextColor(Color.WHITE)
    }

    private fun configurarRecyclerView() {
        // Navegação configurada exatamente com a mesma lógica do RegisterFragment
        alimentoAdapter = AlimentoAdapter(emptyList()) { alimentoSelecionado ->
            val bundle = Bundle().apply {
                putString("FOOD_NAME", alimentoSelecionado.foodName)
                putString("FOOD_DESCRIPTION", alimentoSelecionado.foodDescription)
            }

            if (findNavController().currentDestination?.id == R.id.recentFragment) {
                try {
                    findNavController().navigate(
                        R.id.action_recentFragment_to_addFragment,
                        bundle
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.rvRecentes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alimentoAdapter
        }
    }

    private fun carregarAlimentosRecentes() {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val emailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""

        val jsonBD = JsonBD(requireContext())
        listaAlimentosRecentes = jsonBD.getAlimentosRecentesDoUsuario(emailLogado)

        atualizarExibicaoLista(listaAlimentosRecentes)
    }

    private fun configurarBuscaLocal() {
        binding.etPesquisaRecentes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                val listaFiltrada = if (query.isEmpty()) {
                    listaAlimentosRecentes
                } else {
                    listaAlimentosRecentes.filter {
                        it.foodName.contains(query, ignoreCase = true)
                    }
                }
                atualizarExibicaoLista(listaFiltrada)
            }
        })
    }

    private fun atualizarExibicaoLista(lista: List<FoodItem>) {
        if (lista.isEmpty()) {
            binding.tvVazio.visibility = View.VISIBLE
            binding.rvRecentes.visibility = View.GONE
        } else {
            binding.tvVazio.visibility = View.GONE
            binding.rvRecentes.visibility = View.VISIBLE
            alimentoAdapter.atualizarLista(lista)
        }
    }

    private fun configurarNavegacao() {
        binding.alimentoAba.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.recentFragment) {
                try {
                    findNavController().navigate(R.id.action_recentFragment_to_registerFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.btnCancelar.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.recentFragment) {
                try {
                    findNavController().navigate(R.id.action_recentFragment_to_inicioFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}