
package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentPasswordBinding
import com.example.vita.ui.InputUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null

    private val binding: FragmentPasswordBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPasswordBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura o teclado e a visibilidade das senhas.
        InputUtils.configurarTeclado(view)
        InputUtils.configurarVisibilidadeSenhas(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvarSenha.setOnClickListener {
            alterarSenha()
        }
    }

    private fun alterarSenha() {

        val usuario = FirebaseAuth.getInstance().currentUser
        val email = usuario?.email

        if (usuario == null || email.isNullOrBlank()) {
            mostrarMensagem("Erro: usuário não identificado.")
            return
        }

        val senhaAtual = binding.etSenhaAtual.text.toString()
        val novaSenha = binding.etNovaSenha.text.toString()
        val confirmar = binding.etConfirmarSenha.text.toString()

        if (!validarSenhas(senhaAtual, novaSenha, confirmar)) {
            return
        }

        binding.btnSalvarSenha.isEnabled = false

        autenticarSenhaAtual(
            usuario = usuario,
            email = email,
            senhaAtual = senhaAtual,
            novaSenha = novaSenha
        )
    }
    private fun validarSenhas(
        senhaAtual: String,
        novaSenha: String,
        confirmar: String
    ): Boolean {

        if (
            senhaAtual.isEmpty() ||
            novaSenha.isEmpty() ||
            confirmar.isEmpty()
        ) {
            mostrarMensagem("Preencha todos os campos.")
            return false
        }

        if (novaSenha.length < 6) {
            mostrarMensagem(
                "A nova senha deve ter pelo menos 6 caracteres."
            )
            return false
        }

        if (novaSenha != confirmar) {
            mostrarMensagem(
                "A senha nova e sua confirmação não correspondem."
            )
            return false
        }
        return true
    }

    private fun autenticarSenhaAtual(
        usuario: com.google.firebase.auth.FirebaseUser,
        email: String,
        senhaAtual: String,
        novaSenha: String
    ) {

        val credencial = EmailAuthProvider.getCredential(
            email,
            senhaAtual
        )

        usuario.reauthenticate(credencial)
            .addOnCompleteListener { autenticacao ->

                val tela = _binding ?: return@addOnCompleteListener

                if (!autenticacao.isSuccessful) {
                    tela.btnSalvarSenha.isEnabled = true
                    mostrarMensagem(
                        "A senha atual está incorreta ou a sessão precisa ser renovada.",
                        Toast.LENGTH_LONG
                    )
                    return@addOnCompleteListener
                }
                atualizarSenha(usuario, novaSenha)
            }
    }

    private fun atualizarSenha(
        usuario: com.google.firebase.auth.FirebaseUser,
        novaSenha: String
    ) {

        usuario.updatePassword(novaSenha)
            .addOnCompleteListener { atualizacao ->

                val tela = _binding ?: return@addOnCompleteListener
                tela.btnSalvarSenha.isEnabled = true

                if (atualizacao.isSuccessful) {

                    tela.etSenhaAtual.text?.clear()
                    tela.etNovaSenha.text?.clear()
                    tela.etConfirmarSenha.text?.clear()
                    mostrarMensagem("Senha alterada com sucesso!")

                } else {
                    mostrarMensagem(
                        atualizacao.exception?.localizedMessage
                            ?: "Erro ao atualizar a senha.",
                        Toast.LENGTH_LONG
                    )
                }
            }
    }

    private fun mostrarMensagem(
        mensagem: String,
        duracao: Int = Toast.LENGTH_SHORT
    ) {

        Toast.makeText(
            requireContext(),
            mensagem,
            duracao
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
