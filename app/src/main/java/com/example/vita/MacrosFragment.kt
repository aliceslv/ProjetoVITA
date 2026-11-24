package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.databinding.FragmentMacrosBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MacrosFragment : Fragment() {

    private var _binding: FragmentMacrosBinding? = null

    private val binding: FragmentMacrosBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private var refeicoesListener: ListenerRegistration? = null

    data class MacroDia(
        var carboidratos: Float = 0f,
        var proteinas: Float = 0f,
        var gorduras: Float = 0f
    )

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

        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        atualizarDadosMacros()
    }

    override fun onPause() {
        super.onPause()
        refeicoesListener?.remove()
        refeicoesListener = null
    }

    private fun configurarNavegacao() {

        binding.btnCalorias.setOnClickListener {
            findNavController().navigate(
                R.id.action_macrosFragment_to_caloriasFragment
            )
        }

        binding.btnLivro.setOnClickListener {
            findNavController().navigate(
                R.id.action_macrosFragment_to_inicioFragment
            )
        }

        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(
                R.id.action_macrosFragment_to_profileFragment
            )
        }
    }

    private fun atualizarDadosMacros() {

        refeicoesListener?.remove()
        refeicoesListener = FirestoreData.observarRefeicoes(sucesso = { refeicoes ->

            if (!isAdded) return@observarRefeicoes

            val semana = List(7) {
                MacroDia()
            }

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

                    val alimentos =
                        refeicao["alimentos"] as? List<*> ?: emptyList<Any>()

                    alimentos.forEach { item ->

                        val alimento = item as? Map<*, *>
                            ?: return@forEach

                        semana[indice].carboidratos +=
                            (alimento["carboidratos"] as? Number)?.toFloat()
                                ?: 0f

                        semana[indice].proteinas +=
                            (alimento["proteinas"] as? Number)?.toFloat()
                                ?: 0f

                        semana[indice].gorduras +=
                            (alimento["gorduras"] as? Number)?.toFloat()
                                ?: 0f
                    }
                }
            }

            atualizarMediaDiaria(semana)

            configurarGraficoSemanal(
                binding.chartMacrosSemanal,
                semana
            )
        })
    }

    private fun atualizarMediaDiaria(
        macrosSemanal: List<MacroDia>
    ) {

        val totalCarb = macrosSemanal
            .sumOf { it.carboidratos.toDouble() }
            .toFloat()

        val totalProt = macrosSemanal
            .sumOf { it.proteinas.toDouble() }
            .toFloat()

        val totalGord = macrosSemanal
            .sumOf { it.gorduras.toDouble() }
            .toFloat()

        val mediaCarb = totalCarb / 7f
        val mediaProt = totalProt / 7f
        val mediaGord = totalGord / 7f

        val totalGramas = mediaCarb + mediaProt + mediaGord

        val pctCarb =
            if (totalGramas > 0) {
                (mediaCarb / totalGramas) * 100
            } else {
                0f
            }

        val pctProt =
            if (totalGramas > 0) {
                (mediaProt / totalGramas) * 100
            } else {
                0f
            }

        val pctGord =
            if (totalGramas > 0) {
                (mediaGord / totalGramas) * 100
            } else {
                0f
            }

        binding.txtLabelCarb.text = String.format(
            Locale.getDefault(),
            "Carboidratos (%.0f%%) %.0fg",
            pctCarb,
            mediaCarb
        )

        binding.progressCarb.progress = pctCarb.toInt()

        binding.txtLabelProt.text = String.format(
            Locale.getDefault(),
            "Proteínas (%.0f%%) %.0fg",
            pctProt,
            mediaProt
        )

        binding.progressProt.progress = pctProt.toInt()

        binding.txtLabelGord.text = String.format(
            Locale.getDefault(),
            "Gorduras (%.0f%%) %.0fg",
            pctGord,
            mediaGord
        )

        binding.progressGord.progress = pctGord.toInt()
    }

    private fun configurarGraficoSemanal(
        chart: BarChart,
        macrosSemanal: List<MacroDia>
    ) {

        val barData = criarDadosMacros(macrosSemanal)

        chart.data = barData

        configurarEixoHorizontal(chart)
        configurarEixoVertical(chart)
        configurarLegenda(chart)

        chart.apply {
            description.isEnabled = false
            groupBars(0f, 0.25f, 0.05f)
            invalidate()
        }
    }

    private fun criarDadosMacros(
        macrosSemanal: List<MacroDia>
    ): BarData {

        val entradasCarb = ArrayList<BarEntry>()
        val entradasProt = ArrayList<BarEntry>()
        val entradasGord = ArrayList<BarEntry>()

        macrosSemanal.forEachIndexed { index, macro ->

            entradasCarb.add(
                BarEntry(index.toFloat(), macro.carboidratos)
            )

            entradasProt.add(
                BarEntry(index.toFloat(), macro.proteinas)
            )

            entradasGord.add(
                BarEntry(index.toFloat(), macro.gorduras)
            )
        }

        val conjuntoCarb = criarConjuntoMacros(
            entradasCarb,
            "Carboidratos",
            R.color.green5
        )

        val conjuntoProt = criarConjuntoMacros(
            entradasProt,
            "Proteínas",
            R.color.ciano
        )

        val conjuntoGord = criarConjuntoMacros(
            entradasGord,
            "Gorduras",
            R.color.orange2
        )

        return BarData(
            conjuntoCarb,
            conjuntoProt,
            conjuntoGord
        ).apply {
            barWidth = 0.20f
        }
    }

    private fun criarConjuntoMacros(
        entradas: ArrayList<BarEntry>,
        nome: String,
        cor: Int
    ): BarDataSet {

        return BarDataSet(entradas, nome).apply {
            color = ContextCompat.getColor(
                requireContext(),
                cor
            )
        }
    }

    private fun configurarEixoHorizontal(chart: BarChart) {

        val dias = listOf(
            "Seg", "Ter", "Qua", "Qui",
            "Sex", "Sáb", "Dom"
        )

        val groupSpace = 0.25f
        val barSpace = 0.05f

        chart.xAxis.apply {

            valueFormatter = IndexAxisValueFormatter(dias)

            position = XAxis.XAxisPosition.BOTTOM

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            setDrawGridLines(false)

            granularity = 1f
            isGranularityEnabled = true

            axisMinimum = 0f

            axisMaximum =
                chart.barData.getGroupWidth(groupSpace, barSpace) * 7

            setCenterAxisLabels(true)
        }
    }

    private fun configurarEixoVertical(chart: BarChart) {

        chart.axisLeft.apply {

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            setDrawGridLines(false)

            axisMinimum = 0f
        }

        chart.axisRight.isEnabled = false
    }

    private fun configurarLegenda(chart: BarChart) {

        chart.legend.apply {

            isEnabled = true

            textColor = ContextCompat.getColor(
                requireContext(),
                R.color.white
            )

            textSize = 10f

            form = Legend.LegendForm.SQUARE

            horizontalAlignment =
                Legend.LegendHorizontalAlignment.CENTER

            verticalAlignment =
                Legend.LegendVerticalAlignment.TOP

            orientation =
                Legend.LegendOrientation.HORIZONTAL
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}