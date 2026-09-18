package com.example.vita

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentMacrosBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MacrosFragment : Fragment() {

    private var _binding: FragmentMacrosBinding? = null
    private val binding get() = _binding!!

    data class MacroDia(var carboidratos: Float = 0f, var proteinas: Float = 0f, var gorduras: Float = 0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMacrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuração de Navegação entre os botões da barra superior
        configurarNavegacao()

        // Recarrega os dados do JSON local sempre que a tela é apresentada
        atualizarDadosMacros()
    }

    override fun onResume() {
        super.onResume()
        // Garante a atualização imediata caso o registro tenha mudado
        atualizarDadosMacros()
    }

    private fun configurarNavegacao() {
        binding.btnCalorias.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.macrosFragment, CaloriasFragment())
                .commit()
        }

        binding.btnNutrientes.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.macrosFragment, NutriFragment())
                .commit()
        }
        // --- BARRA INFERIOR DE NAVEGAÇÃO ---

        // Ícone do Livro -> InicioFragment
        binding.btnLivro.setOnClickListener {
            findNavController().navigate(R.id.action_macrosFragment_to_inicioFragment)
        }

        // Ícone do Usuário -> ProfileFragment
        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(R.id.action_macrosFragment_to_profileFragment)
        }
    }

    private fun atualizarDadosMacros() {
        val email = obterEmailUsuarioLogado()
        val macrosSemanal = carregarMacrosSemanalDoJson(email)

        // 1. Atualizar barras de Média Diária
        atualizarMediaDiaria(macrosSemanal)

        // 2. Desenhar Gráfico de Distribuição Semanal com 3 Barras agrupadas
        configurarGraficoSemanal(binding.chartMacrosSemanal, macrosSemanal)
    }

    private fun carregarMacrosSemanalDoJson(email: String): List<MacroDia> {
        val listaSemanal = List(7) { MacroDia() }
        val file = File(requireContext().filesDir, "refeicoes.json")

        if (!file.exists()) return listaSemanal

        try {
            val jsonArray = JSONArray(file.readText())
            val calendar = Calendar.getInstance()
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // Gera datas para a semana corrente (Segunda a Domingo)
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

                if (usuarioEmail.equals(email, ignoreCase = true)) {
                    val indexDia = datasSemana.indexOf(dataRefeicao)
                    if (indexDia != -1) {
                        val alimentos = refeicao.optJSONArray("alimentos") ?: JSONArray()
                        for (j in 0 until alimentos.length()) {
                            val alimento = alimentos.getJSONObject(j)
                            listaSemanal[indexDia].carboidratos += alimento.optDouble("carboidratos", 0.0).toFloat()
                            listaSemanal[indexDia].proteinas += alimento.optDouble("proteinas", 0.0).toFloat()
                            listaSemanal[indexDia].gorduras += alimento.optDouble("gorduras", 0.0).toFloat()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listaSemanal
    }

    private fun atualizarMediaDiaria(macrosSemanal: List<MacroDia>) {
        val totalCarb = macrosSemanal.sumOf { it.carboidratos.toDouble() }.toFloat()
        val totalProt = macrosSemanal.sumOf { it.proteinas.toDouble() }.toFloat()
        val totalGord = macrosSemanal.sumOf { it.gorduras.toDouble() }.toFloat()

        // Média em gramas considerando 7 dias da semana
        val mediaCarb = totalCarb / 7f
        val mediaProt = totalProt / 7f
        val mediaGord = totalGord / 7f

        val totalGemas = mediaCarb + mediaProt + mediaGord

        val pctCarb = if (totalGemas > 0) (mediaCarb / totalGemas) * 100 else 0f
        val pctProt = if (totalGemas > 0) (mediaProt / totalGemas) * 100 else 0f
        val pctGord = if (totalGemas > 0) (mediaGord / totalGemas) * 100 else 0f

        binding.txtLabelCarb.text = String.format(Locale.getDefault(), "Carboidratos (%.0f%%) %.0fg", pctCarb, mediaCarb)
        binding.progressCarb.progress = pctCarb.toInt()

        binding.txtLabelProt.text = String.format(Locale.getDefault(), "Proteínas (%.0f%%) %.0fg", pctProt, mediaProt)
        binding.progressProt.progress = pctProt.toInt()

        binding.txtLabelGord.text = String.format(Locale.getDefault(), "Gorduras (%.0f%%) %.0fg", pctGord, mediaGord)
        binding.progressGord.progress = pctGord.toInt()
    }

    private fun configurarGraficoSemanal(chart: BarChart, macrosSemanal: List<MacroDia>) {
        val entriesCarb = ArrayList<BarEntry>()
        val entriesProt = ArrayList<BarEntry>()
        val entriesGord = ArrayList<BarEntry>()

        macrosSemanal.forEachIndexed { index, macro ->
            entriesCarb.add(BarEntry(index.toFloat(), macro.carboidratos))
            entriesProt.add(BarEntry(index.toFloat(), macro.proteinas))
            entriesGord.add(BarEntry(index.toFloat(), macro.gorduras))
        }

        val setCarb = BarDataSet(entriesCarb, "Carboidratos").apply {
            color = Color.parseColor("#20D36B")
            setDrawValues(false)
        }
        val setProt = BarDataSet(entriesProt, "Proteínas").apply {
            color = Color.parseColor("#27B7C8")
            setDrawValues(false)
        }
        val setGord = BarDataSet(entriesGord, "Gorduras").apply {
            color = Color.parseColor("#FFA800")
            setDrawValues(false)
        }

        val groupSpace = 0.25f
        val barSpace = 0.05f
        val barWidth = 0.20f
        // Cálculo do formato: (barWidth + barSpace) * 3 + groupSpace = (0.20 + 0.05)*3 + 0.25 = 1.00

        val barData = BarData(setCarb, setProt, setGord)
        barData.barWidth = barWidth

        chart.data = barData

        // Eixo X
        val dias = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dias)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = true
            axisMinimum = 0f
            axisMaximum = 0f + chart.barData.getGroupWidth(groupSpace, barSpace) * 7
            setCenterAxisLabels(true)
        }

        // Eixo Y
        chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
            axisMinimum = 0f
        }
        chart.axisRight.isEnabled = false

        // Configuração da Legenda
        chart.legend.apply {
            isEnabled = true
            textColor = Color.WHITE
            textSize = 10f
            form = Legend.LegendForm.SQUARE
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            orientation = Legend.LegendOrientation.HORIZONTAL
        }

        chart.apply {
            description.isEnabled = false
            groupBars(0f, groupSpace, barSpace)
            invalidate()
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