package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.repository.CustomFoodRepository
import com.example.vita.databinding.FragmentCustomFoodBinding
import com.example.vita.ui.InputUtils

class CustomFoodFragment : Fragment() {

    private var _binding: FragmentCustomFoodBinding? = null

    private val binding: FragmentCustomFoodBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val repository = CustomFoodRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomFoodBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InputUtils.configurarTeclado(view)

        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvarAlimento.setOnClickListener {
            validarEAdicionar()
        }
    }

    private fun validarEAdicionar() {

        val nome = binding.etNomeAlimento.text.toString().trim()

        val calorias = converterNumero(
            binding.etCalorias.text.toString()
        )

        val proteinas = converterNumero(
            binding.etProteinas.text.toString()
        )

        val carboidratos = converterNumero(
            binding.etCarboidratos.text.toString()
        )

        val gorduras = converterNumero(
            binding.etGorduras.text.toString()
        )

        if (nome.isBlank()) {
            mostrarMensagem("Informe o nome do alimento.")
            return
        }

        if (
            calorias == null ||
            proteinas == null ||
            carboidratos == null ||
            gorduras == null
        ) {
            mostrarMensagem("Preencha todos os valores nutricionais.")
            return
        }

        if (
            calorias < 0 ||
            proteinas < 0 ||
            carboidratos < 0 ||
            gorduras < 0
        ) {
            mostrarMensagem("Os valores não podem ser negativos.")
            return
        }

        binding.btnSalvarAlimento.isEnabled = false

        repository.salvarAlimento(
            nome = nome,
            calorias = calorias,
            proteinas = proteinas,
            carboidratos = carboidratos,
            gorduras = gorduras,
            sucesso = {

                if (!isAdded) return@salvarAlimento

                mostrarMensagem("Alimento cadastrado com sucesso!")

                findNavController().navigateUp()
            },
            erro = { erro ->

                if (!isAdded) return@salvarAlimento

                binding.btnSalvarAlimento.isEnabled = true

                mostrarMensagem(
                    erro.localizedMessage
                        ?: "Não foi possível cadastrar o alimento."
                )
            }
        )
    }

    private fun converterNumero(valor: String): Double? {
        return valor.trim()
            .replace(",", ".")
            .toDoubleOrNull()
    }

    private fun mostrarMensagem(mensagem: String) {

        Toast.makeText(
            requireContext(),
            mensagem,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
