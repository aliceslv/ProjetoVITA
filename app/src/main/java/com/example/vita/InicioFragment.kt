package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.vita.data.FirestoreData
import com.example.vita.databinding.FragmentInicioBinding
import com.example.vita.ui.BannerAdapter
import com.example.vita.ui.BannerData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InicioFragment : Fragment() {

    private data class ResumoNutrientes(
        val gordura: Double,
        val carboidratos: Double,
        val proteinas: Double,
        val calorias: Double
    )

    private var _binding: FragmentInicioBinding? = null

    private val binding: FragmentInicioBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private lateinit var bannerAdapter: BannerAdapter

    private var perfilListener: ListenerRegistration? = null
    private var refeicoesListener: ListenerRegistration? = null
    private var aguaListener: ListenerRegistration? = null

    private val dataSelecionada = Calendar.getInstance()

    private var perfilAtual: Map<String, Any> = emptyMap()
    private var refeicoesAtuais: List<Map<String, Any>> = emptyList()
    private var registrosAguaAtuais: List<Map<String, Any>> = emptyList()

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

        configurarViewPagerBanners()
        configurarNavegacao()
        configurarNavegacaoDatas()

        atualizarDataAtual()
    }

    override fun onResume() {
        super.onResume()

        iniciarObservadores()
    }

    override fun onPause() {
        super.onPause()
        perfilListener?.remove()
        refeicoesListener?.remove()
        aguaListener?.remove()
        perfilListener = null
        refeicoesListener = null
        aguaListener = null
    }

    private fun configurarViewPagerBanners() {
        bannerAdapter = BannerAdapter(emptyList()) {
            findNavController().navigate(
                R.id.action_inicioFragment_to_caloriasFragment
            )
        }

        binding.pagerBanner.adapter = bannerAdapter
        binding.arrowPrev.setOnClickListener {

            val current = binding.pagerBanner.currentItem
            if (current > 0) {
                binding.pagerBanner.currentItem = current - 1
            }
        }

        binding.arrowNext.setOnClickListener {
            val current = binding.pagerBanner.currentItem
            val total = binding.pagerBanner.adapter?.itemCount ?: 0

            if (current < total - 1) {
                binding.pagerBanner.currentItem = current + 1
            }
        }

        binding.pagerBanner.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    atualizarIndicadores(position)

                    if (position == 1) {
                        binding.frameBanner.backgroundTintList =
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.blue8
                            )
                    } else {
                        binding.frameBanner.backgroundTintList =
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.blue4
                            )
                    }
                }
            }
        )
    }

    private fun configurarNavegacao() {
        binding.cardRegistrarRefeicoes.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_registerFragment
            )
        }
        binding.cardAgua.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_aguaFragment
            )
        }
        binding.btnScale.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_caloriasFragment
            )
        }
        binding.btnUser.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_profileFragment
            )
        }
    }

    private fun configurarNavegacaoDatas() {
        binding.btnDiaAnterior.setOnClickListener {
            alterarData(-1)
        }
        binding.btnProximoDia.setOnClickListener {
            alterarData(1)
        }
    }

    private fun alterarData(dias: Int) {

        val hoje = Calendar.getInstance()
        val proximaData = dataSelecionada.clone() as Calendar

        proximaData.add(Calendar.DAY_OF_MONTH, dias)

        // Impede a navegação para datas futuras.
        if (
            proximaData.get(Calendar.YEAR) > hoje.get(Calendar.YEAR) ||
            (
                    proximaData.get(Calendar.YEAR) == hoje.get(Calendar.YEAR) &&
                            proximaData.get(Calendar.DAY_OF_YEAR) > hoje.get(Calendar.DAY_OF_YEAR)
                    )
        ) {
            return
        }

        dataSelecionada.time = proximaData.time

        atualizarDataSelecionada()
        atualizarResumoDiario()
    }

    private fun atualizarDataSelecionada() {

        val formato = SimpleDateFormat(
            "EEEE, d 'de' MMMM",
            Locale("pt", "BR")
        )

        val dataFormatada = formato.format(dataSelecionada.time)

        binding.txtData.text = dataFormatada.replaceFirstChar {
            it.titlecase(Locale("pt", "BR"))
        }

        val hoje = Calendar.getInstance()
        val ehHoje =
            dataSelecionada.get(Calendar.YEAR) == hoje.get(Calendar.YEAR) &&
                    dataSelecionada.get(Calendar.DAY_OF_YEAR) == hoje.get(Calendar.DAY_OF_YEAR)

        binding.txtRefeicoesHoje.text =
            if (ehHoje) "Refeições de hoje" else "Refeições do dia"

        binding.btnProximoDia.isEnabled = !ehHoje

        binding.btnProximoDia.alpha =
            if (ehHoje) 0.4f else 1f
    }
    private fun atualizarDataAtual() {
        dataSelecionada.time = Calendar.getInstance().time
        atualizarDataSelecionada()
    }

    private fun iniciarObservadores() {
        perfilListener?.remove()
        refeicoesListener?.remove()
        aguaListener?.remove()

        perfilListener = FirestoreData.observarPerfil(sucesso = { perfil ->
            if (!isAdded) return@observarPerfil
            perfilAtual = perfil

            val nome = perfilAtual["nome"]?.toString() ?: "Usuário"
            val idr = (perfilAtual["idr"] as? Number)?.toInt()
                ?: perfilAtual["meta_calorica"]?.toString()?.toIntOrNull()
                ?: 2000

            binding.txtSaudacao.text = "Olá, $nome!"
            binding.txtIdrValor.text = idr.toString()
            atualizarResumoDiario()
        })

        refeicoesListener = FirestoreData.observarRefeicoes(sucesso = { refeicoes ->
            if (!isAdded) return@observarRefeicoes
            refeicoesAtuais = refeicoes
            atualizarResumoDiario()
        })

        aguaListener = FirestoreData.observarAgua(sucesso = { registros ->
            if (!isAdded) return@observarAgua
            registrosAguaAtuais = registros
            atualizarResumoDiario()
        })
    }

    private fun obterIntervaloDia(): Pair<Date, Date> {
        val calendario = dataSelecionada.clone() as Calendar

        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)

        val inicio = calendario.time
        calendario.add(Calendar.DAY_OF_MONTH, 1)

        val fim = calendario.time
        return Pair(inicio, fim)
    }

    private fun obterAlimentosDoDia(
        inicioDia: Date,
        fimDia: Date
    ): List<Map<*, *>> {

        val alimentosDoDia = mutableListOf<Map<*, *>>()

        refeicoesAtuais.forEach { refeicao ->
            val data = (refeicao["data"] as? Timestamp)?.toDate()

            if (
                data != null &&
                !data.before(inicioDia) &&
                data.before(fimDia)
            ) {

                val alimentos = refeicao["alimentos"] as? List<*>
                    ?: emptyList<Any>()

                alimentos.forEach { item ->
                    val alimento = item as? Map<*, *>
                    if (alimento != null) {
                        alimentosDoDia.add(alimento)
                    }
                }
            }
        }

        return alimentosDoDia
    }
    private fun calcularNutrientes(
        alimentos: List<Map<*, *>>
    ): ResumoNutrientes {

        fun somar(campo: String): Double {
            return alimentos.sumOf { alimento ->
                (alimento[campo] as? Number)?.toDouble() ?: 0.0
            }
        }
        return ResumoNutrientes(
            gordura = somar("gorduras"),
            carboidratos = somar("carboidratos"),
            proteinas = somar("proteinas"),
            calorias = somar("calorias")
        )
    }
    private fun calcularAguaDoDia(
        inicioDia: Date,
        fimDia: Date
    ): Int {

        return registrosAguaAtuais.sumOf { registro ->
            val data = (registro["data"] as? Timestamp)?.toDate()

            if (
                data != null &&
                !data.before(inicioDia) &&
                data.before(fimDia)
            ) {
                (registro["quantidade_ml"] as? Number)?.toInt() ?: 0
            } else {
                0
            }
        }
    }
    private fun exibirResumo(
        resumo: ResumoNutrientes,
        aguaHoje: Int,
        metaAgua: Int
    ) {

        binding.txtGordura.text = "${resumo.gordura.toInt()}g"
        binding.txtCarbo.text = "${resumo.carboidratos.toInt()}g"
        binding.txtProteina.text = "${resumo.proteinas.toInt()}g"

        binding.txtCaloriasRefeicoes.text =
            "${resumo.calorias.toInt()} kcal"

        binding.txtAgua.text = "$aguaHoje / $metaAgua"

        atualizarBanner(resumo.calorias)
    }

    private fun atualizarResumoDiario() {

        val (inicioDia, fimDia) = obterIntervaloDia()
        val alimentos = obterAlimentosDoDia(inicioDia, fimDia)
        val resumo = calcularNutrientes(alimentos)
        val aguaHoje = calcularAguaDoDia(inicioDia, fimDia)

        val peso = (perfilAtual["peso"] as? Number)?.toDouble()
            ?: perfilAtual["peso"]?.toString()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
            ?: 70.0

        val metaAgua = (perfilAtual["metaAgua"] as? Number)?.toInt()
            ?: (peso * 35).toInt()

        exibirResumo(resumo, aguaHoje, metaAgua)
    }

    private fun atualizarBanner(calorias: Double) {

        val idr = (perfilAtual["idr"] as? Number)?.toInt()
            ?: perfilAtual["meta_calorica"]?.toString()?.toIntOrNull()
            ?: 2000

        val banners = listOf(
            BannerData(
                tipo = 1,
                metaCalorias = idr,
                consumidas = calorias.toInt()
            ),
            BannerData(
                tipo = 2,
                metaCalorias = 0,
                consumidas = 0
            )
        )

        bannerAdapter.atualizarDados(banners)
        criarIndicadores(banners.size)
    }

    private fun criarIndicadores(count: Int) {

        val layout = binding.layoutIndicadores
        layout.removeAllViews()
        for (i in 0 until count) {

            val dot = ImageView(requireContext()).apply {

                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.circle
                    )
                )

                val params = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(6, 0, 6, 0)
                }
                layoutParams = params
                alpha = if (i == 0) 1.0f else 0.4f
            }
            layout.addView(dot)
        }
    }

    private fun atualizarIndicadores(posicao: Int) {
        val layout = binding.layoutIndicadores

        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            child.alpha = if (i == posicao) 1.0f else 0.4f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}