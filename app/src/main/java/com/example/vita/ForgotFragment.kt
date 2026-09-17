package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentForgotBinding
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.example.vita.json.JsonBD

class ForgotFragment : Fragment() {

    private var _binding: FragmentForgotBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var dbManager: JsonBD

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

        auth = FirebaseAuth.getInstance()
        dbManager = JsonBD(requireContext())

        // Volta para a tela anterior
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.forgotbtn.setOnClickListener {
            val email = binding.loginInput.text.toString().trim()

            if (email.isEmpty()) {
                binding.loginInput.error = "Por favor, preencha o campo de e-mail"
                binding.loginInput.requestFocus()
                return@setOnClickListener
            }

            // 1. Valida se o e-mail existe no banco de dados JSON local
            if (!dbManager.emailExists(email)) {
                binding.loginInput.error = "Este e-mail não está cadastrado"
                binding.loginInput.requestFocus()
                Toast.makeText(
                    requireContext(),
                    "E-mail não encontrado no banco de dados local.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 2. Dispara e-mail via Firebase
            auth.useAppLanguage()

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://vita-sendemail.firebaseapp.com/__/auth/action")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    requireContext().packageName,
                    true,
                    "21"
                )
                .build()

            auth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Link enviado! Verifique sua caixa de entrada.",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    } else {
                        val erro = task.exception?.localizedMessage ?: "Erro desconhecido"
                        Toast.makeText(
                            requireContext(),
                            "Falha ao enviar: $erro",
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