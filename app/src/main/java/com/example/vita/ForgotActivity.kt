package com.example.vita

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityForgotBinding
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbManager: JsonBD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbManager = JsonBD(this)

        binding.icarrow.setOnClickListener {
            finish()
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
                    this,
                    "E-mail não encontrado no banco de dados local.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 2. Se o e-mail existe no JSON, dispara o e-mail via Firebase
            auth.useAppLanguage()

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://vita-sendemail.firebaseapp.com/__/auth/action")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    packageName,
                    true,
                    "21"
                )
                .build()

            auth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Link enviado! Verifique sua caixa de entrada.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        val erro = task.exception?.localizedMessage ?: "Erro desconhecido"
                        Toast.makeText(
                            baseContext,
                            "Falha ao enviar: $erro",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}