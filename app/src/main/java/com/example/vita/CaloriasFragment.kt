package com.example.vita

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vita.databinding.FragmentCaloriasBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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

        // 1. Busca a IDR armazenada no SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val idrArmazenada = sharedPref.getFloat("USER_IDR", 2000f) // 2000f como fallback

        // 2. Atualiza o texto da Meta na tela com a IDR armazenada
        binding.txtMedia.text = "Meta: ${idrArmazenada.toInt()} kcal"

        // 3. Exemplo do consumo diário registrado na semana (Segunda a Domingo)
        val consumoSemanal = listOf(1500f, 1800f, 1600f, 2100f, 1950f, 1700f, 1400f)

        // Atualiza o valor consumido do dia atual (exemplo: último dia da lista)
        val consumoHoje = consumoSemanal.lastOrNull() ?: 0f
        binding.txt1400.text = consumoHoje.toInt().toString()

        // 4. Desenha o gráfico usando a IDR armazenada como a linha de meta
        configurarGrafico(binding.chartMetaConsumo, idrArmazenada, consumoSemanal)
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

        // Define a linha da Meta no gráfico com a IDR armazenada
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

        // Eixo X (Dias)
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