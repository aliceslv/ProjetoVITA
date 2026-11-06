package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentSettingsBinding
import com.example.vita.json.JsonBD
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura a navegação dos botões
        binding.btnFechar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPrivacidade.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacidadeFragment)
        }

        binding.btnDadosPessoais.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_atualizaFragment)
        }

        binding.btnSobre.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        }

        // Ação do Botão Logout (Sair da Conta)
        binding.btnLogout.setOnClickListener {
            realizarLogout()
        }
    }

    // Chamado sempre que o Fragment se torna visível (atualização em tempo real ao voltar de outras telas)
    override fun onResume() {
        super.onResume()
        carregarDadosUsuario()
    }

    private fun obterEmailUsuarioLogado(): String {
        if (userViewModel.email.isNotEmpty()) {
            return userViewModel.email
        }
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "") ?: ""
        if (email.isNotEmpty()) {
            userViewModel.email = email
        }
        return email
    }

    private fun carregarDadosUsuario() {
        val emailAtual = obterEmailUsuarioLogado()
        if (emailAtual.isEmpty()) return

        val jsonBd = JsonBD(requireContext())
        val usersArray = jsonBd.getUsers()

        for (i in 0 until usersArray.length()) {
            val userObj: JSONObject = usersArray.getJSONObject(i)
            if (userObj.optString("email").equals(emailAtual, ignoreCase = true)) {
                val nome = userObj.optString("nome", "Usuário")
                val email = userObj.optString("email", emailAtual)

                // Atualiza o front-end em tempo real
                binding.nomeUser.text = nome
                binding.emailUser.text = email
                break
            }
        }
    }

    private fun realizarLogout() {
        FirebaseAuth.getInstance().signOut()

        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        sharedPref.edit().remove("USER_EMAIL").apply()

        // 3. Limpa o ViewModel
        userViewModel.email = ""
        userViewModel.nome = ""

        try {
            findNavController().navigate(R.id.action_settingsFragment_to_startFragment)
        } catch (e: Exception) {
            // Fallback caso o ID da rota seja diferente no seu nav_graph
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}