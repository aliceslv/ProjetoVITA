package com.example.vita

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vita.databinding.ActivityResetBinding
import com.google.firebase.auth.FirebaseAuth

class ResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetBinding
    private lateinit var auth: FirebaseAuth
    private var oobCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o View Binding
        binding = ActivityResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Botão de voltar
        binding.icarrow.setOnClickListener { finish() }

        // Captura o link que abriu a Activity e extrai o oobCode
        val data: Uri? = intent.data
        if (data != null) {
            // Tenta pegar o oobCode diretamente do parâmetro da URL
            oobCode = data.getQueryParameter("oobCode")

            // Fallback: se o link veio encapsulado pelo Firebase Dynamic Links
            if (oobCode.isNullOrEmpty()) {
                val deepLink = data.getQueryParameter("link")
                if (!deepLink.isNullOrEmpty()) {
                    val innerUri = Uri.parse(deepLink)
                    oobCode = innerUri.getQueryParameter("oobCode")
                }
            }
        }

        // Valida se o código de verificação existe
        if (oobCode.isNullOrEmpty()) {
            Toast.makeText(this, "Link de redefinição inválido ou expirado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Evento de clique no botão de redefinir
        binding.resetBtn.setOnClickListener {
            val newPassword = binding.senhaInput.text.toString().trim()

            // Validação simples da senha
            if (newPassword.isEmpty() || newPassword.length < 6) {
                binding.senhaInput.error = "A senha deve conter pelo menos 6 caracteres"
                binding.senhaInput.requestFocus()
                return@setOnClickListener
            }

            // Confirma a alteração de senha no Firebase Auth
            auth.confirmPasswordReset(oobCode!!, newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Senha alterada com sucesso! Faça login com a nova senha.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        val erro = task.exception?.localizedMessage ?: "Erro ao redefinir senha."
                        Toast.makeText(this, "Falha: $erro", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}