package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentInicioBinding
import com.example.vita.json.JsonBD
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega todos os dados sempre que a Fragment ganha foco
        atualizarDataAtual()
        carregarDadosUsuario()
        carregarResumoDiario()
    }

    private fun configurarNavegacao() {
        // Redireciona para RegisterFragment ao clicar no card de refeições
        binding.cardRegistrarRefeicoes.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_registerFragment)
        }

        // Navegação da Barra Inferior
        binding.btnScale.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_caloriasFragment)
        }

        binding.btnUser.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_profileFragment)
        }
    }

    private fun atualizarDataAtual() {
        // Formata e exibe a data atual do sistema (Ex: "Quinta-feira, 17 de Setembro")
        val formatoData = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val dataFormatada = formatoData.format(Date())

        // Coloca a primeira letra do dia da semana em maiúsculo
        binding.txtData.text = dataFormatada.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString()
        }
    }

    private fun carregarDadosUsuario() {
        val email = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        var nomeEncontrado = "Usuário"
        var idrEncontrado = 2000

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(email, ignoreCase = true)) {
                nomeEncontrado = user.optString("nome", "Usuário")
                idrEncontrado = user.optInt("idr", 2000)
                break
            }
        }

        binding.txtSaudacao.text = "Olá, $nomeEncontrado!"
        binding.txtIdrValor.text = idrEncontrado.toString()
    }

    private fun carregarResumoDiario() {
        val email = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())

        // Data atual no formato ISO (YYYY-MM-DD)
        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hojeIso = formatoIso.format(Date())

        var totalGordura = 0.0
        var totalCarbo = 0.0
        var totalProteina = 0.0
        var totalCalorias = 0.0

        try {
            val jsonArray = jsonBD.getRefeicoes()

            for (i in 0 until jsonArray.length()) {
                val refeicao = jsonArray.getJSONObject(i)
                val usuarioRefeicao = refeicao.optString("usuarioEmail")
                val dataRefeicao = jsonBD.formatarDataParaIso(refeicao.optString("data"))

                // Filtra apenas registros do usuário logado e que coincidem com a data de HOJE
                if (usuarioRefeicao.equals(email, ignoreCase = true) && dataRefeicao == hojeIso) {
                    val alimentos = refeicao.optJSONArray("alimentos") ?: JSONArray()
                    for (j in 0 until alimentos.length()) {
                        val alimento = alimentos.getJSONObject(j)
                        totalGordura += alimento.optDouble("gorduras", 0.0)
                        totalCarbo += alimento.optDouble("carboidratos", 0.0)
                        totalProteina += alimento.optDouble("proteinas", 0.0)
                        totalCalorias += alimento.optDouble("calorias", 0.0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Atualiza a interface do card de macronutrientes e calorias diárias
        binding.txtGordura.text = "${totalGordura.toInt()}g"
        binding.txtCarbo.text = "${totalCarbo.toInt()}g"
        binding.txtProteina.text = "${totalProteina.toInt()}g"
        binding.txtCaloriasRefeicoes.text = "${totalCalorias.toInt()} kcal"
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