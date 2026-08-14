package com.example.vita

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityForgotBinding
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {

    // Declaração do Binding e da Auth do Firebase
    private lateinit var binding: ActivityForgotBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout e define como a view principal
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Seta de voltar
        binding.icarrow.setOnClickListener {
            finish()
        }

        // Botão para enviar e-mail
        binding.forgotbtn.setOnClickListener {
            val email = binding.loginInput.text.toString().trim()

            if (email.isEmpty()) {
                binding.loginInput.error = "Por favor, preencha o campo de e-mail"
                binding.loginInput.requestFocus()
                return@setOnClickListener
            }

            auth.useAppLanguage()

            // Configuração para forçar a abertura do app via link
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://vita-sendemail.firebaseapp.com/__/auth/action") // Seu domínio do Firebase
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    packageName, // Pega o nome do pacote atual dinamicamente
                    true,        // Tenta instalar pela Play Store caso não tenha o app
                    "21"         // SDK Mínimo
                )
                .build()

            // Envia o e-mail passando a configuração do Deep Link
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