package com.example.vita

import android.content.Context
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
import com.example.vita.ui.BannerAdapter
import com.example.vita.databinding.FragmentInicioBinding
import com.example.vita.json.JsonBD
import com.example.vita.ui.BannerData
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerAdapter: BannerAdapter

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
    }

    override fun onResume() {
        super.onResume()
        atualizarDataAtual()
        carregarDadosUsuario()
        carregarResumoDiario()
    }

    private fun configurarViewPagerBanners() {
        bannerAdapter = BannerAdapter(emptyList()) {
            findNavController().navigate(R.id.action_inicioFragment_to_caloriasFragment)
        }

        binding.pagerBanner.adapter = bannerAdapter

        // Setas para navegar entre os banners
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

        // Atualização dos indicadores e da cor de fundo ao deslizar o banner
        binding.pagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                atualizarIndicadores(position)

                if (position == 1) {
                    binding.frameBanner.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue8)
                } else {
                    binding.frameBanner.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue4)
                }
            }
        })
    }

    private fun configurarNavegacao() {
        binding.cardRegistrarRefeicoes.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_registerFragment)
        }

        binding.btnScale.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_caloriasFragment)
        }

        binding.btnUser.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_profileFragment)
        }
    }

    private fun atualizarDataAtual() {
        val formatoData = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        val dataFormatada = formatoData.format(Date())

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

        val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hojeIso = formatoIso.format(Date())

        var totalGordura = 0.0
        var totalCarbo = 0.0
        var totalProteina = 0.0
        var totalCalorias = 0.0
        var idrMeta = 2000

        // Busca a meta IDR do usuário
        val users = jsonBD.getUsers()
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(email, ignoreCase = true)) {
                idrMeta = user.optInt("idr", 2000)
                break
            }
        }

        try {
            val jsonArray = jsonBD.getRefeicoes()

            for (i in 0 until jsonArray.length()) {
                val refeicao = jsonArray.getJSONObject(i)
                val usuarioRefeicao = refeicao.optString("usuarioEmail")
                val dataRefeicao = jsonBD.formatarDataParaIso(refeicao.optString("data"))

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

        // Atualização da UI dos cards de macros
        binding.txtGordura.text = "${totalGordura.toInt()}g"
        binding.txtCarbo.text = "${totalCarbo.toInt()}g"
        binding.txtProteina.text = "${totalProteina.toInt()}g"
        binding.txtCaloriasRefeicoes.text = "${totalCalorias.toInt()} kcal"

        // Lista contendo os dois banners (Banner 1 e Banner 2)
        val listaBanners = listOf(
            BannerData(
                tipo = 1,
                metaCalorias = idrMeta,
                consumidas = totalCalorias.toInt()
            ),
            BannerData(
                tipo = 2,
                metaCalorias = 0,
                consumidas = 0
            )
        )

        bannerAdapter.atualizarDados(listaBanners)
        criarIndicadores(listaBanners.size)
    }

    private fun criarIndicadores(count: Int) {
        val layout = binding.layoutIndicadores
        layout.removeAllViews()

        for (i in 0 until count) {
            val dot = ImageView(requireContext()).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (i == 0) R.drawable.circle else R.drawable.circle
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

    private fun obterEmailUsuarioLogado(): String {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}