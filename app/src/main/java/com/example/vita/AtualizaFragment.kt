package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentAtualizaBinding
import com.example.vita.json.JsonBD
import org.json.JSONObject

class AtualizaFragment : Fragment() {

    private var _binding: FragmentAtualizaBinding? = null
    private val binding get() = _binding!!

    // Compartilhando o ViewModel com a Activity
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAtualizaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Carrega e exibe os dados atuais do usuário
        carregarDadosAtuais()

        // 2. Navegação do botão voltar (btnBack) para o SettingsFragment
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 3. Botão Salvar (Apenas atualiza o JSON e o ViewModel, SEM navegar)
        binding.btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun carregarDadosAtuais() {
        val emailAtual = obterEmailUsuarioLogado().ifEmpty { userViewModel.email }
        if (emailAtual.isEmpty()) return

        val jsonBd = JsonBD(requireContext())
        val usersArray = jsonBd.getUsers()

        var encontrou = false

        for (i in 0 until usersArray.length()) {
            val userObj: JSONObject = usersArray.getJSONObject(i)
            if (userObj.optString("email").equals(emailAtual, ignoreCase = true)) {
                val nome = userObj.optString("nome", "")
                val email = userObj.optString("email", "")
                val nascimento = userObj.optString("nascimento", "")

                // Atribuição direta aos componentes do layout via binding
                binding.nameUser.text = nome
                binding.emailUsuario.text = email // Atualiza explicitamente o TextView do e-mail

                binding.etNome.setText(nome)
                binding.etEmail.setText(email)
                binding.etDataNasc.setText(nascimento)

                encontrou = true
                break
            }
        }

        // Se por acaso o e-mail não estiver exato no JSON mas estiver no ViewModel/SharedPreferences,
        // garante que pelo menos o e-mail atual aparece na tela
        if (!encontrou) {
            binding.emailUsuario.text = emailAtual
            binding.etEmail.setText(emailAtual)
        }
    }

    private fun salvarAlteracoes() {
        val emailAntigo = obterEmailUsuarioLogado().ifEmpty { userViewModel.email }
        val novoNome = binding.etNome.text.toString().trim()
        val novoEmail = binding.etEmail.text.toString().trim()
        val novaDataNasc = binding.etDataNasc.text.toString().trim()

        if (novoNome.isEmpty() || novoEmail.isEmpty() || novaDataNasc.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBd = JsonBD(requireContext())
        val sucesso = jsonBd.atualizarDadosPessoais(emailAntigo, novoNome, novoEmail, novaDataNasc)

        if (sucesso) {
            // Atualiza o SharedPreferences caso o e-mail tenha sido alterado
            val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
            sharedPref.edit().putString("USER_EMAIL", novoEmail).apply()

            // Atualiza também os dados no ViewModel para refletir instantaneamente nas outras telas
            userViewModel.nome = novoNome
            userViewModel.email = novoEmail

            // Atualiza os textos do cartão na mesma tela instantaneamente (Nome e E-mail)
            binding.nameUser.text = novoNome
            binding.emailUsuario.text = novoEmail

            Toast.makeText(requireContext(), "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Erro ao atualizar os dados.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obterEmailUsuarioLogado(): String {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}