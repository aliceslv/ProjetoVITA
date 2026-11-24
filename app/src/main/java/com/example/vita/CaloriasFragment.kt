package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.databinding.FragmentCaloriasBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CaloriasFragment : Fragment() {

    private var _binding: FragmentCaloriasBinding? = null

    private val binding: FragmentCaloriasBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private var perfilListener: ListenerRegistration? = null
    private var refeicoesListener: ListenerRegistration? = null

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
        carregarEDesenharDados()
    }

    override fun onPause() {
        super.onPause()
        perfilListener?.remove()
        refeicoesListener?.remove()
        perfilListener = null
        refeicoesListener = null
    }

    private fun configurarNavegacao() {

        binding.btnMacros.setOnClickListener {
            findNavController().navigate(
                R.id.action_caloriasFragment_to_macrosFragment
            )
        }

        binding.btnLivro.setOnClickListener {
            findNavController().navigate(
                R.id.action_caloriasFragment_to_inicioFragment
            )
        }

        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(
                R.id.action_caloriasFragment_to_profileFragment
            )
        }
    }

    private fun carregarEDesenharDados() {

        perfilListener?.remove()
        refeicoesListener?.remove()

        perfilListener = FirestoreData.observarPerfil(sucesso = { perfil ->

            if (!isAdded) return@observarPerfil

            refeicoesListener?.remove()

            val idr = (perfil["idr"] as? Number)?.toFloat()
                ?: perfil["meta_calorica"]?.toString()?.toFloatOrNull()
                ?: 2000f

            refeicoesListener = FirestoreData.observarRefeicoes(sucesso = { refeicoes ->

                if (!isAdded) return@observarRefeicoes

                val consumo = MutableList(7) { 0f }

                val calendario = Calendar.getInstance()

                calendario.firstDayOfWeek = Calendar.MONDAY

                calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                val datas = mutableListOf<String>()

                val formato = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )

                repeat(7) {

                    datas.add(formato.format(calendario.time))

                    calendario.add(Calendar.DAY_OF_MONTH, 1)
                }

                refeicoes.forEach { refeicao ->

                    val data =
                        (refeicao["data"] as? Timestamp)?.toDate()
                            ?: return@forEach

                    val indice = datas.indexOf(formato.format(data))

                    if (indice >= 0) {

                        val total =
                            (refeicao["total_calorias"] as? Number)?.toFloat()
                                ?: (
                                        (refeicao["alimentos"] as? List<*>)
                                            ?.sumOf { item ->

                                                (
                                                        (item as? Map<*, *>)?.get("calorias")
                                                                as? Number
                                                        )?.toDouble() ?: 0.0
                                            }?.toFloat() ?: 0f
                                        )

                        consumo[indice] += total
                    }
                }

                val media = consumo.average().toFloat()

                binding.txtMedia.text =
                    "Média: ${media.toInt()} kcal"

                binding.txt1400.text =
                    "${consumo[obterIndiceDiaAtualSemana()].toInt()} kcal"

                configurarGrafico(
                    binding.chartMetaConsumo,
                    idr,
                    consumo
                )
            })
        })
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

        chart.data = criarDadosCalorias(consumoSemanal)

        configurarLinhaMeta(chart, idrMeta)
        configurarEixosCalorias(chart)
        configurarAparenciaCalorias(chart)

        chart.animateY(1000)
        chart.invalidate()
    }

    private fun criarDadosCalorias(
        consumoSemanal: List<Float>
    ): BarData {

        val entradas = consumoSemanal.mapIndexed { index, consumo ->

            BarEntry(index.toFloat(), consumo)
        }

        val conjunto = BarDataSet(
            entradas,
            "Consumo Diário (kcal)"
        ).apply {

            color = ContextCompat.getColor(
                requireContext(),
                R.color.ciano
            )

            valueTextColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            valueTextSize = 10f
        }

        return BarData(conjunto).apply {
            barWidth = 0.45f
        }
    }

    private fun configurarLinhaMeta(
        chart: BarChart,
        idrMeta: Float
    ) {

        val linhaMeta = LimitLine(
            idrMeta,
            "Meta (${idrMeta.toInt()} kcal)"
        ).apply {

            lineWidth = 2f

            lineColor = ContextCompat.getColor(
                requireContext(),
                R.color.orange2
            )

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            textSize = 11f

            enableDashedLine(10f, 10f, 0f)
        }

        chart.axisLeft.apply {
            removeAllLimitLines()
            addLimitLine(linhaMeta)
        }
    }

    private fun configurarEixosCalorias(chart: BarChart) {

        chart.axisLeft.apply {

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            setDrawGridLines(false)
        }

        chart.axisRight.isEnabled = false

        val dias = listOf(
            "Seg", "Ter", "Qua", "Qui",
            "Sex", "Sáb", "Dom"
        )

        chart.xAxis.apply {

            valueFormatter = IndexAxisValueFormatter(dias)

            position = XAxis.XAxisPosition.BOTTOM

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            setDrawGridLines(false)

            granularity = 1f
        }
    }

    private fun configurarAparenciaCalorias(chart: BarChart) {

        chart.apply {

            description.isEnabled = false

            legend.textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            setFitBars(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}