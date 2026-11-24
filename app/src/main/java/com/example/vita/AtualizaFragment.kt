package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestoreData
import com.example.vita.ui.InputUtils
import com.example.vita.databinding.FragmentAtualizaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions

class AtualizaFragment : Fragment() {

    private var _binding: FragmentAtualizaBinding? = null

    private val binding: FragmentAtualizaBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAtualizaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        InputUtils.configurarTeclado(view)

        carregarDadosAtuais()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun carregarDadosAtuais() {

        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        FirestoreData.buscarPerfil(sucesso = { dados ->

            if (!isAdded) return@buscarPerfil

            val nome = dados["nome"]?.toString().orEmpty()

            val email = usuario.email
                ?: dados["email"]?.toString().orEmpty()

            val nascimento = dados["nascimento"]?.toString().orEmpty()

            binding.nameUser.text = nome
            binding.emailUsuario.text = email

            binding.etNome.setText(nome)
            binding.etEmail.setText(email)
            binding.etDataNasc.setText(nascimento)
        })
    }

    private fun salvarAlteracoes() {

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            mostrarMensagem("Usuário não autenticado.")
            return
        }

        val nome = binding.etNome.text.toString().trim()
        val novoEmail = binding.etEmail.text.toString()
            .trim().lowercase(Locale.ROOT)
        val nascimento = binding.etDataNasc.text.toString().trim()

        if (!validarCampos(nome, novoEmail, nascimento)) {
            mostrarMensagem("Preencha todos os campos!")
            return
        }

        binding.btnSalvar.isEnabled = false

        if (novoEmail.equals(usuario.email, ignoreCase = true)) {
            salvarNoFirestore(usuario.uid, nome, novoEmail, nascimento)
        } else {
            alterarEmail(usuario, nome, novoEmail, nascimento)
        }
    }

    private fun validarCampos(
        nome: String,
        email: String,
        nascimento: String
    ): Boolean {

        return nome.isNotEmpty() &&
                email.isNotEmpty() &&
                nascimento.isNotEmpty()
    }

    private fun alterarEmail(
        usuario: com.google.firebase.auth.FirebaseUser,
        nome: String,
        novoEmail: String,
        nascimento: String
    ) {

        usuario.updateEmail(novoEmail)
            .addOnCompleteListener { tarefa ->

                if (!isAdded) return@addOnCompleteListener

                if (tarefa.isSuccessful) {

                    salvarNoFirestore(
                        usuario.uid,
                        nome,
                        novoEmail,
                        nascimento
                    )

                } else {

                    binding.btnSalvar.isEnabled = true

                    mostrarMensagem(
                        tarefa.exception?.localizedMessage
                            ?: "Não foi possível alterar o e-mail. Entre novamente e tente."
                    )
                }
            }
    }

    private fun salvarNoFirestore(
        uid: String,
        nome: String,
        email: String,
        nascimento: String
    ) {

        val dados = hashMapOf<String, Any>(
            "nome" to nome,
            "email" to email,
            "nascimento" to nascimento
        )

        FirestoreData.usuario(uid)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {

                if (!isAdded) return@addOnSuccessListener

                binding.btnSalvar.isEnabled = true

                userViewModel.nome = nome
                userViewModel.email = email

                binding.nameUser.text = nome
                binding.emailUsuario.text = email

                mostrarMensagem("Dados atualizados com sucesso!")
            }
            .addOnFailureListener { erro ->

                if (!isAdded) return@addOnFailureListener

                binding.btnSalvar.isEnabled = true

                mostrarMensagem(
                    "Erro ao atualizar: ${erro.localizedMessage}"
                )
            }
    }

    private fun mostrarMensagem(mensagem: String) {

        Toast.makeText(
            requireContext(),
            mensagem,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}