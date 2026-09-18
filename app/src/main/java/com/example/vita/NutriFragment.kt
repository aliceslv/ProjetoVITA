package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentNutriBinding
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NutriFragment : Fragment() {

    private var _binding: FragmentNutriBinding? = null
    private val binding get() = _binding!!

    data class NutrientesTotais(
        var fibras: Float = 0f,
        var acucar: Float = 0f,
        var sodio: Float = 0f,
        var colesterol: Float = 0f,
        var gordSaturada: Float = 0f
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNutriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacao()
        atualizarNutrientes()
    }

    override fun onResume() {
        super.onResume()
        atualizarNutrientes()
    }

    private fun configurarNavegacao() {
        binding.btnCalorias.setOnClickListener {
            findNavController().navigate(R.id.action_nutriFragment_to_caloriasFragment)
        }

        binding.btnMacros.setOnClickListener {
            findNavController().navigate(R.id.action_nutriFragment_to_macrosFragment)
        }

        // Ícone do Livro -> InicioFragment
        binding.btnLivro.setOnClickListener {
            findNavController().navigate(R.id.action_caloriasFragment_to_inicioFragment)
        }

        // Ícone do Usuário -> ProfileFragment
        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_caloriasFragment_to_profileFragment)
        }
    }

    private fun atualizarNutrientes() {
        val email = obterEmailUsuarioLogado()
        val totais = carregarNutrientesSemanalDoJson(email)

        // Calcula a média diária considerando os 7 dias da semana
        val mediaFibras = totais.fibras / 7f
        val mediaAcucar = totais.acucar / 7f
        val mediaSodio = totais.sodio / 7f
        val mediaColesterol = totais.colesterol / 7f
        val mediaGordSaturada = totais.gordSaturada / 7f

        // Atualização da UI para Fibras (Meta: 25g)
        binding.txtValFibras.text = String.format(Locale.getDefault(), "%.0f / 25g", mediaFibras)
        binding.progressFibras.progress = mediaFibras.toInt().coerceAtMost(25)

        // Atualização da UI para Açúcar (Meta: 50g)
        binding.txtValAcucar.text = String.format(Locale.getDefault(), "%.0f / 50g", mediaAcucar)
        binding.progressAcucar.progress = mediaAcucar.toInt().coerceAtMost(50)

        // Atualização da UI para Sódio (Meta: 2300mg)
        binding.txtValSodio.text = String.format(Locale.getDefault(), "%.0f / 2300mg", mediaSodio)
        binding.progressSodio.progress = mediaSodio.toInt().coerceAtMost(2300)

        // Atualização da UI para Colesterol (Meta: 300mg)
        binding.txtValColesterol.text = String.format(Locale.getDefault(), "%.0f / 300mg", mediaColesterol)
        binding.progressColesterol.progress = mediaColesterol.toInt().coerceAtMost(300)

        // Atualização da UI para Gordura Saturada (Meta: 20g)
        binding.txtValGordSaturada.text = String.format(Locale.getDefault(), "%.0f / 20g", mediaGordSaturada)
        binding.progressGordSaturada.progress = mediaGordSaturada.toInt().coerceAtMost(20)
    }

    private fun carregarNutrientesSemanalDoJson(email: String): NutrientesTotais {
        val totais = NutrientesTotais()
        val file = File(requireContext().filesDir, "refeicoes.json")

        if (!file.exists()) return totais

        try {
            val jsonArray = JSONArray(file.readText())
            val calendar = Calendar.getInstance()
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            val datasSemana = mutableListOf<String>()
            for (i in 0..6) {
                datasSemana.add(formatoData.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            for (i in 0 until jsonArray.length()) {
                val refeicao = jsonArray.getJSONObject(i)
                val usuarioEmail = refeicao.optString("usuarioEmail")
                val dataRefeicao = refeicao.optString("data")

                if (usuarioEmail.equals(email, ignoreCase = true) && datasSemana.contains(dataRefeicao)) {
                    val alimentos = refeicao.optJSONArray("alimentos") ?: JSONArray()
                    for (j in 0 until alimentos.length()) {
                        val alimento = alimentos.getJSONObject(j)
                        totais.fibras += alimento.optDouble("fibras", 0.0).toFloat()
                        totais.acucar += alimento.optDouble("acucar", 0.0).toFloat()
                        totais.sodio += alimento.optDouble("sodio", 0.0).toFloat()
                        totais.colesterol += alimento.optDouble("colesterol", 0.0).toFloat()
                        totais.gordSaturada += alimento.optDouble("gorduraSaturada", 0.0).toFloat()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return totais
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