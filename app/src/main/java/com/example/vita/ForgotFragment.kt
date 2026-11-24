package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentForgotBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.vita.ui.InputUtils

class ForgotFragment : Fragment() {

    private var _binding: FragmentForgotBinding? = null

    private val binding: FragmentForgotBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotBinding.inflate(inflater, container, false)
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

        binding.forgotbtn.setOnClickListener {

            val email = binding.loginInput.text.toString().trim()

            if (email.isEmpty()) {
                binding.loginInput.error =
                    "Por favor, preencha o campo de e-mail"

                binding.loginInput.requestFocus()
                return@setOnClickListener
            }

            binding.forgotbtn.isEnabled = false

            auth.useAppLanguage()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->

                    if (!isAdded) return@addOnCompleteListener

                    binding.forgotbtn.isEnabled = true

                    if (task.isSuccessful) {

                        Toast.makeText(
                            requireContext(),
                            "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir a senha.",
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigateUp()

                    } else {

                        Toast.makeText(
                            requireContext(),
                            task.exception?.localizedMessage
                                ?: "Não foi possível enviar o e-mail.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}