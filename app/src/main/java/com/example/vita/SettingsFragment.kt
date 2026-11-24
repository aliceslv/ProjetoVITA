package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val binding: FragmentSettingsBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFechar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPrivacidade.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_privacidadeFragment
            )
        }

        binding.btnDadosPessoais.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_atualizaFragment
            )
        }

        binding.btnSobre.setOnClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_aboutFragment
            )
        }

        binding.btnLogout.setOnClickListener {
            realizarLogout()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDadosUsuario()
    }

    private fun carregarDadosUsuario() {

        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        FirestoreData.buscarPerfil(sucesso = { dados ->

            if (!isAdded) return@buscarPerfil

            val nome = dados["nome"]?.toString() ?: "Usuário"

            val email = usuario.email
                ?: dados["email"]?.toString().orEmpty()

            binding.nomeUser.text = nome
            binding.emailUser.text = email

            userViewModel.nome = nome
            userViewModel.email = email
        })
    }

    private fun realizarLogout() {

        FirebaseAuth.getInstance().signOut()

        userViewModel.email = ""
        userViewModel.nome = ""
        userViewModel.senha = ""

        findNavController().navigate(
            R.id.action_settingsFragment_to_startFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}