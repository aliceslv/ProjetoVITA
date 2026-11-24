package com.example.vita

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.example.vita.ui.InputUtils
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vita.data.FirestoreData
import com.example.vita.data.network.FoodItem
import com.example.vita.databinding.FragmentRecentBinding
import com.example.vita.ui.AlimentoAdapter
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFragment : Fragment() {

    private var _binding: FragmentRecentBinding? = null

    private val binding: FragmentRecentBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private lateinit var alimentoAdapter: AlimentoAdapter

    private var listaAlimentosRecentes: List<FoodItem> = emptyList()
    private var refeicoesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InputUtils.configurarTeclado(view)

        binding.dataReal.text = SimpleDateFormat(
            "EEEE, MMM. dd",
            Locale("pt", "BR")
        ).format(Date())

        binding.consumoAba.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.green2
            )
        )

        binding.alimentoAba.setTextColor(Color.WHITE)

        configurarRecyclerView()
        configurarNavegacao()
        configurarBuscaLocal()
    }

    override fun onResume() {
        super.onResume()
        carregarAlimentosRecentes()
    }

    override fun onPause() {
        super.onPause()
        refeicoesListener?.remove()
        refeicoesListener = null
    }

    private fun configurarRecyclerView() {

        alimentoAdapter = AlimentoAdapter(emptyList()) { alimento ->

            val bundle = Bundle().apply {

                putString("FOOD_NAME", alimento.foodName)
                putString("FOOD_DESCRIPTION", alimento.foodDescription)

                val origem = when {
                    alimento.foodId.startsWith("recent_personalizado_") ->
                        "personalizado"

                    alimento.foodId.startsWith("recent_fatsecret_") ->
                        "fatsecret"

                    else ->
                        "desconhecida"
                }

                putString("FOOD_ORIGIN", origem)
            }

            if (
                findNavController().currentDestination?.id ==
                R.id.recentFragment
            ) {
                findNavController().navigate(
                    R.id.action_recentFragment_to_addFragment,
                    bundle
                )
            }
        }

        binding.rvRecentes.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvRecentes.adapter = alimentoAdapter
    }


    private fun carregarAlimentosRecentes() {

        refeicoesListener?.remove()

        refeicoesListener = FirestoreData.observarRefeicoes(
            sucesso = { refeicoes ->

                if (!isAdded) return@observarRefeicoes

                val vistos = mutableSetOf<String>()
                val recentes = mutableListOf<FoodItem>()

                refeicoes.forEach { refeicao ->

                    val alimentos =
                        refeicao["alimentos"] as? List<*> ?: emptyList<Any>()

                    alimentos.forEach alimentoLoop@{ item ->

                        val alimento = item as? Map<*, *>
                            ?: return@alimentoLoop

                        val nome = alimento["nome"]?.toString().orEmpty()

                        if (nome.isBlank()) return@alimentoLoop

                        val origem =
                            alimento["origem"]?.toString() ?: "desconhecida"

                        // Permite alimentos de origens diferentes
                        // mesmo quando possuem o mesmo nome.
                        val identificador = "${origem}_${nome.lowercase(Locale.ROOT)}"

                        if (!vistos.add(identificador)) {
                            return@alimentoLoop
                        }

                        val gramas =
                            (alimento["gramas"] as? Number)?.toDouble()
                                ?: (alimento["quantidade"] as? Number)?.toDouble()
                                ?: 100.0

                        val calorias =
                            (alimento["calorias"] as? Number)?.toDouble() ?: 0.0

                        val carboidratos =
                            (alimento["carboidratos"] as? Number)?.toDouble() ?: 0.0

                        val proteinas =
                            (alimento["proteinas"] as? Number)?.toDouble() ?: 0.0

                        val gorduras =
                            (alimento["gorduras"] as? Number)?.toDouble() ?: 0.0

                        val descricao = """
                        Serving Size: $gramas g
                        Calories: $calorias kcal
                        Carbs: $carboidratos g
                        Protein: $proteinas g
                        Fat: $gorduras g
                    """.trimIndent()

                        recentes.add(
                            FoodItem(
                                foodId = "recent_${origem}_${nome}",
                                foodName = nome,
                                foodDescription = descricao,
                                foodType = "generic"
                            )
                        )
                    }
                }

                listaAlimentosRecentes = recentes

                atualizarExibicaoLista(recentes)

            },
            erro = {

                if (isAdded) {
                    atualizarExibicaoLista(emptyList())
                }
            }
        )
    }


    private fun configurarBuscaLocal() {

        binding.etPesquisaRecentes.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {

                    val query = s?.toString()?.trim() ?: ""

                    val filtrada =
                        if (query.isEmpty()) {
                            listaAlimentosRecentes
                        } else {
                            listaAlimentosRecentes.filter { alimento ->
                                alimento.foodName.indexOf(
                                    query,
                                    ignoreCase = true
                                ) >= 0
                            }
                        }

                    atualizarExibicaoLista(filtrada)
                }
            }
        )
    }

    private fun atualizarExibicaoLista(lista: List<FoodItem>) {

        binding.tvVazio.visibility =
            if (lista.isEmpty()) View.VISIBLE else View.GONE

        binding.rvRecentes.visibility =
            if (lista.isEmpty()) View.GONE else View.VISIBLE

            alimentoAdapter.atualizarLista(lista)

    }

    private fun configurarNavegacao() {

        binding.alimentoAba.setOnClickListener {

            if (
                findNavController().currentDestination?.id ==
                R.id.recentFragment
            ) {
                findNavController().navigate(
                    R.id.action_recentFragment_to_registerFragment
                )
            }
        }

        binding.btnCancelar.setOnClickListener {

            if (
                findNavController().currentDestination?.id ==
                R.id.recentFragment
            ) {
                findNavController().navigate(
                    R.id.action_recentFragment_to_inicioFragment
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}