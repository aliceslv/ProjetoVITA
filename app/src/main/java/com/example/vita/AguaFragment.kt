package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.vita.ui.InputUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.data.repository.AguaRepository
import com.example.vita.databinding.FragmentAguaBinding

class AguaFragment : Fragment() {

    private var _binding: FragmentAguaBinding? = null

    private val binding: FragmentAguaBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAguaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        InputUtils.configurarTeclado(view)

        configurarNavegacao()
        carregarMetaAgua()
        configurarBotaoSalvar()
    }

    private fun configurarNavegacao() {

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.root.findViewById<View?>(R.id.btnCancelar)
            ?.setOnClickListener {
                findNavController().navigateUp()
            }
    }

    private fun carregarMetaAgua() {

        FirestoreData.buscarPerfil(

            sucesso = { perfil ->

                if (!isAdded || _binding == null) return@buscarPerfil

                val peso = (perfil["peso"] as? Number)?.toDouble()
                    ?: perfil["peso"]?.toString()
                        ?.replace(",", ".")
                        ?.toDoubleOrNull()
                    ?: 70.0

                val meta = (perfil["metaAgua"] as? Number)?.toInt()
                    ?: (peso * 35).toInt()

                binding.tvMetaCalculada.text =
                    "$meta ml (baseada no seu peso de ${peso.toInt()}kg)"
            },

            erro = {

                if (!isAdded || _binding == null) return@buscarPerfil

                binding.tvMetaCalculada.text = "Meta diária: 2450 ml"
            }
        )
    }

    private fun configurarBotaoSalvar() {

        binding.btnSalvarAgua.setOnClickListener {

            val quantidade = binding.etQuantidadeMl.text
                .toString()
                .trim()
                .toIntOrNull()

            if (quantidade == null || quantidade <= 0) {

                Toast.makeText(
                    requireContext(),
                    "Informe uma quantidade válida!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val uid = FirestoreData.uid()

            if (uid == null) {

                Toast.makeText(
                    requireContext(),
                    "Usuário não autenticado.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            salvarRegistroAgua(uid, quantidade)
        }
    }

    private fun salvarRegistroAgua(
        uid: String,
        quantidade: Int
    ) {

        binding.btnSalvarAgua.isEnabled = false

        AguaRepository().salvarRegistro(
            uid,
            quantidade,

            sucesso = {

                if (!isAdded || _binding == null) {
                    return@salvarRegistro
                }

                binding.btnSalvarAgua.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Registro de água salvo!",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigateUp()
            },

            erro = { erro ->

                if (!isAdded || _binding == null) {
                    return@salvarRegistro
                }

                binding.btnSalvarAgua.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar registro: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}