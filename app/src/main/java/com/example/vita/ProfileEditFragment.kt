package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentProfileEditBinding
import com.example.vita.json.JsonBD
import java.util.Locale

class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carregarDadosEAtualizarImc()
        configurarAcoes()
    }

    private fun carregarDadosEAtualizarImc() {
        val emailLogado = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(emailLogado, ignoreCase = true)) {

                val pesoStr = user.optString("peso", "0")
                val alturaStr = user.optString("altura", "0")
                val pesoMetaStr = user.optString("pesoMeta", "0")

                // Preenche os campos de texto com os dados atuais
                binding.inputPesoAtual.setText(pesoStr)
                binding.inputAltura.setText(alturaStr)
                binding.inputPesoMeta.setText(pesoMetaStr)

                // Calcula e exibe o IMC
                calcularEExibirImc(pesoStr, alturaStr)

                break
            }
        }
    }

    private fun calcularEExibirImc(pesoStr: String, alturaStr: String) {
        val peso = pesoStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val alturaCm = alturaStr.replace(",", ".").toDoubleOrNull() ?: 0.0

        if (peso > 0.0 && alturaCm > 0.0) {
            // Converte altura de centímetros para metros
            val alturaMetros = alturaCm / 100.0

            // Fórmula do IMC: Peso / (Altura * Altura)
            val imc = peso / (alturaMetros * alturaMetros)

            // Exibe o valor do IMC formatado com 1 casa decimal
            binding.txtValorImc.text = String.format(Locale.US, "%.1f", imc)

            // Classificação do IMC
            val (status, corResId) = when {
                imc < 18.5 -> Pair("Abaixo do peso", R.color.orange)
                imc in 18.5..24.9 -> Pair("Peso normal", R.color.green1)
                imc in 25.0..29.9 -> Pair("Sobrepeso", R.color.orange)
                imc in 30.0..34.9 -> Pair("Obesidade I", R.color.orange)
                imc in 35.0..39.9 -> Pair("Obesidade II", R.color.orange)
                else -> Pair("Obesidade III", R.color.orange)
            }

            binding.txtStatusImc.text = status
            binding.txtStatusImc.setTextColor(requireContext().getColor(corResId))
        } else {
            binding.txtValorImc.text = "--"
            binding.txtStatusImc.text = "Sem dados"
        }
    }

    private fun salvarNovasMedidas() {
        val emailLogado = obterEmailUsuarioLogado()
        val novoPeso = binding.inputPesoAtual.text.toString().trim()
        val novaAltura = binding.inputAltura.text.toString().trim()
        val novoPesoMeta = binding.inputPesoMeta.text.toString().trim()

        if (novoPeso.isEmpty() || novaAltura.isEmpty() || novoPesoMeta.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBD = JsonBD(requireContext())
        val sucesso = jsonBD.atualizarMedidasUsuario(
            email = emailLogado,
            novoPeso = novoPeso,
            novaAltura = novaAltura,
            novoPesoMeta = novoPesoMeta
        )

        if (sucesso) {
            Toast.makeText(requireContext(), "Medidas atualizadas!", Toast.LENGTH_SHORT).show()
            // Recarrega os dados da tela e recalcula o IMC com os valores novos salvos
            carregarDadosEAtualizarImc()
        } else {
            Toast.makeText(requireContext(), "Erro ao atualizar medidas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarAcoes() {
        // Botão Fechar (Ícone da seta no topo) -> Retorna para a tela de Perfil
        binding.icFechar.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botão Salvar Medidas -> Salva no JSON e Recarrega a tela
        binding.btnSalvarMedidas.setOnClickListener {
            salvarNovasMedidas()
        }
    }

    private fun obterEmailUsuarioLogado(): String {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}