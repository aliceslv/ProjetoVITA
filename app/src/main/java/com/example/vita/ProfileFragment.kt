package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.databinding.FragmentProfileBinding
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding: FragmentProfileBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        carregarDadosPerfil()
    }

    private fun carregarDadosPerfil() {

        FirestoreData.buscarPerfil(sucesso = { dados ->

            if (!isAdded) return@buscarPerfil

            binding.tvName.text =
                dados["nome"]?.toString() ?: "Usuário"

            val peso = dados["peso"]?.toString() ?: "0"

            binding.tvPesoValor.text = peso

            val criado = dados["createdAt"] as? Timestamp

            val dias = criado?.let {

                (
                        TimeUnit.MILLISECONDS.toDays(
                            System.currentTimeMillis() - it.toDate().time
                        ) + 1
                        ).coerceAtLeast(1)

            } ?: 1

            binding.tvFocoValor.text = dias.toString()
        })
    }

    private fun configurarNavegacao() {

        binding.btnEditarPerfil.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_profileEditFragment
            )
        }

        binding.btnLembretes.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_lembretesFragment
            )
        }

        binding.btnConfiguracoes.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_settingsFragment
            )
        }

        binding.btnLivro.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_inicioFragment
            )
        }

        binding.btnEscala.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_caloriasFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}