package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentLoginBinding
import com.example.vita.json.JsonBD
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbManager: JsonBD
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbManager = JsonBD(requireContext())
        auth = FirebaseAuth.getInstance()

        // Botão voltar
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        // Navegação para a tela de Cadastro
        binding.createBtn2.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment)
        }

        // Navegação para a tela de Recuperar Senha
        binding.forgotBtn3.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotFragment)
        }

        // Lógica de autenticação ao clicar em "Entrar"
        binding.loginBtn.setOnClickListener {
            processarLogin()
        }
    }

    private fun processarLogin() {
        val email = binding.edtEmail.text.toString().trim()
        val senha = binding.edtSenha.text.toString()

        if (email.isEmpty()) {
            binding.edtEmail.error = "Digite seu e-mail"
            binding.edtEmail.requestFocus()
            return
        }

        if (senha.isEmpty()) {
            binding.edtSenha.error = "Digite sua senha"
            binding.edtSenha.requestFocus()
            return
        }

        // Desabilita o botão temporariamente para evitar cliques duplos
        binding.loginBtn.isEnabled = false

        // 1. Tenta autenticar no Firebase Auth
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    salvarSessaoENavegar(email)
                } else {
                    // 2. Se o Firebase falhar ou o app estiver offline, valida via JSON local
                    val loginJsonValido = dbManager.validateLogin(email, senha)

                    if (loginJsonValido) {
                        salvarSessaoENavegar(email)
                    } else {
                        binding.loginBtn.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "E-mail ou senha incorretos!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun salvarSessaoENavegar(email: String) {
        // Salva o e-mail nas SharedPreferences para ser usado em outras fragments
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        sharedPref.edit().putString("USER_EMAIL", email).apply()

        Toast.makeText(
            requireContext(),
            "Login realizado com sucesso!",
            Toast.LENGTH_SHORT
        ).show()

        // Navega APENAS se o login for válido
        findNavController().navigate(R.id.action_loginFragment_to_inicioFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}