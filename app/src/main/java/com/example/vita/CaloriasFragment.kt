package com.example.vita

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentCaloriasBinding
import com.example.vita.json.JsonBD
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CaloriasFragment : Fragment() {

    private var _binding: FragmentCaloriasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaloriasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        // Garante a leitura e atualização dos dados sempre que a tela ganha foco
        carregarEDesenharDados()
    }

    private fun configurarNavegacao() {
        // Abas superiores
        binding.btnMacros.setOnClickListener {
            findNavController().navigate(R.id.action_caloriasFragment_to_macrosFragment)
        }

        // --- BARRA INFERIOR DE NAVEGAÇÃO ---

        // Ícone do Livro -> InicioFragment
        binding.btnLivro.setOnClickListener {
            findNavController().navigate(R.id.action_caloriasFragment_to_inicioFragment)
        }

        // Ícone do Usuário -> ProfileFragment
        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_caloriasFragment_to_profileFragment)
        }
    }

    private fun carregarEDesenharDados() {
        val emailUsuario = obterEmailUsuarioLogado()

        // 1. Carrega a IDR armazenada no cadastro do usuário
        val idrMeta = carregarIdrDoJson(emailUsuario)

        // 2. Carrega as calorias consumidas na semana
        val consumoSemanal = carregarConsumoSemanalDoJson(emailUsuario)

        // 3. Atualiza os textos exibidos na tela
        val mediaSemanal = if (consumoSemanal.isNotEmpty()) consumoSemanal.average().toFloat() else 0f
        binding.txtMedia.text = "Média: ${mediaSemanal.toInt()} kcal"

        val indiceHoje = obterIndiceDiaAtualSemana()
        val consumoHoje = consumoSemanal.getOrElse(indiceHoje) { 0f }
        binding.txt1400.text = "${consumoHoje.toInt()} kcal"

        // 4. Desenha o gráfico de barras com a linha de limite da meta
        configurarGrafico(binding.chartMetaConsumo, idrMeta, consumoSemanal)
    }

    private fun carregarIdrDoJson(email: String): Float {
        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(email, ignoreCase = true)) {
                return user.optDouble("idr", 2000.0).toFloat()
            }
        }
        return 2000f
    }

    private fun carregarConsumoSemanalDoJson(email: String): List<Float> {
        val consumoDias = MutableList(7) { 0f }
        val jsonBD = JsonBD(requireContext())

        try {
            val jsonArray = jsonBD.getRefeicoes()
            val calendar = Calendar.getInstance()
            val formatoDataIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            // Mapeia os 7 dias da semana atual (Segunda a Domingo) em formato ISO
            val datasSemana = mutableListOf<String>()
            for (i in 0..6) {
                datasSemana.add(formatoDataIso.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            for (i in 0 until jsonArray.length()) {
                val refeicao = jsonArray.getJSONObject(i)
                val usuarioRefeicao = refeicao.optString("usuarioEmail")
                val dataRefeicaoRaw = refeicao.optString("data")
                val dataRefeicao = jsonBD.formatarDataParaIso(dataRefeicaoRaw)

                if (usuarioRefeicao.equals(email, ignoreCase = true)) {
                    val indiceDia = datasSemana.indexOf(dataRefeicao)
                    if (indiceDia != -1) {
                        val alimentos = refeicao.optJSONArray("alimentos") ?: JSONArray()
                        var caloriasRefeicao = 0f

                        for (j in 0 until alimentos.length()) {
                            val alimento = alimentos.getJSONObject(j)
                            caloriasRefeicao += alimento.optDouble("calorias", 0.0).toFloat()
                        }

                        consumoDias[indiceDia] += caloriasRefeicao
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return consumoDias
    }

    private fun obterEmailUsuarioLogado(): String {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", "") ?: ""
    }

    private fun obterIndiceDiaAtualSemana(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    private fun configurarGrafico(
        chart: BarChart,
        idrMeta: Float,
        consumoSemanal: List<Float>
    ) {
        val entradas = consumoSemanal.mapIndexed { index, consumo ->
            BarEntry(index.toFloat(), consumo)
        }

        val dataSet = BarDataSet(entradas, "Consumo Diário (kcal)").apply {
            color = Color.parseColor("#27B7C8")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.45f
        }

        chart.data = barData

        val eixoYEsquerda = chart.axisLeft
        eixoYEsquerda.removeAllLimitLines()

        val linhaMeta = LimitLine(idrMeta, "Meta (${idrMeta.toInt()} kcal)").apply {
            lineWidth = 2f
            lineColor = Color.parseColor("#FFA800")
            textColor = Color.WHITE
            textSize = 11f
            enableDashedLine(10f, 10f, 0f)
        }

        eixoYEsquerda.addLimitLine(linhaMeta)
        eixoYEsquerda.textColor = Color.WHITE
        eixoYEsquerda.setDrawGridLines(false)

        chart.axisRight.isEnabled = false

        val dias = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dias)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
            granularity = 1f
        }

        chart.apply {
            description.isEnabled = false
            legend.textColor = Color.WHITE
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}