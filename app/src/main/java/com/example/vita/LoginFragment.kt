package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbManager: JsonBD

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

        // Botão voltar (substitui o finish())
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

        // Lógica de validação de Login
        binding.loginBtn.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString()

            if (email.isEmpty()) {
                binding.edtEmail.error = "Digite seu e-mail"
                binding.edtEmail.requestFocus()
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                binding.edtSenha.error = "Digite sua senha"
                binding.edtSenha.requestFocus()
                return@setOnClickListener
            }

            val loginValido = dbManager.validateLogin(email, senha)

            if (loginValido) {
                Toast.makeText(
                    requireContext(),
                    "Login realizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                // Exemplo de navegação para a Home após o login bem-sucedido:
                // findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "E-mail ou senha incorretos!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}