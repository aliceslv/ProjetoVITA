package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.vita.ui.InputUtils

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding: FragmentLoginBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

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

        InputUtils.configurarTeclado(view)
        InputUtils.configurarVisibilidadeSenhas(view)

        auth = FirebaseAuth.getInstance()

        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.createBtn2.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_cadastroFragment
            )
        }

        binding.forgotBtn3.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_forgotFragment
            )
        }

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

        binding.loginBtn.isEnabled = false

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->

                if (!isAdded) return@addOnCompleteListener

                binding.loginBtn.isEnabled = true

                if (task.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "Login realizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(
                        R.id.action_loginFragment_to_inicioFragment
                    )

                } else {

                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage
                            ?: "Não foi possível entrar.",
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