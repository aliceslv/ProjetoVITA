package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentRecentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFragment : Fragment() {

    private var _binding: FragmentRecentBinding? = null
    private val binding get() = _binding!!

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

        // 1. Atualiza a data em tempo real
        atualizarDataAtual()

        // 2. Define o estilo das abas (Consumido Recentemente = Verde)
        configurarEstiloCabecalho()

        // 3. Configura o clique nos botões e abas
        configurarNavegacao()
    }

    private fun atualizarDataAtual() {
        val formatoData = SimpleDateFormat("EEEE, MMM. dd", Locale("pt", "BR"))
        val dataFormatada = formatoData.format(Date())

        // Atualiza a TextView da data (segunda TextView do cabeçalho)
        binding.dataReal.text = dataFormatada
    }

    private fun configurarEstiloCabecalho() {
        val corVerde = ContextCompat.getColor(requireContext(), R.color.green2)
        val corBranca = Color.WHITE

        // Na tela Recentes:
        // "CONSUMIDO RECENTEMENTE" fica VERDE
        // "ALIMENTO" fica BRANCO
        binding.consumoAba.setTextColor(corVerde)
        binding.alimentoAba.setTextColor(corBranca)
    }

    private fun configurarNavegacao() {
        // Clicar na aba "ALIMENTO" volta para a RegisterFragment
        binding.alimentoAba.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.recentFragment) {
                try {
                    findNavController().navigate(R.id.action_recentFragment_to_registerFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Botão "Cancelar" navega direto para a InicioFragment
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