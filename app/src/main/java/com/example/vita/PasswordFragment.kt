package com.example.vita

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentPasswordBinding
import com.example.vita.json.JsonBD

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    // Variáveis para controlar a visibilidade da senha (olhinho)
    private var senhaAtualVisivel = false
    private var novaSenhaVisivel = false
    private var confirmarSenhaVisivel = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarVisibilidadeSenhas()

        // O botão voltar navega de volta para privacidadefragment
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvarSenha.setOnClickListener {
            val emailLogado = obterEmailUsuarioLogado()
            val senhaAtualDigitada = binding.etSenhaAtual.text.toString().trim()
            val novaSenhaDigitada = binding.etNovaSenha.text.toString().trim()
            val confirmarSenhaDigitada = binding.etConfirmarSenha.text.toString().trim()

            if (emailLogado.isEmpty()) {
                Toast.makeText(requireContext(), "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jsonBD = JsonBD(requireContext())
            val users = jsonBD.getUsers()

            var senhaRealNoJson = ""
            var usuarioEncontrado = false

            // Busca a senha real armazenada no JSON para o usuário atual
            for (i in 0 until users.length()) {
                val user = users.getJSONObject(i)
                if (user.optString("email").equals(emailLogado, ignoreCase = true)) {
                    senhaRealNoJson = user.optString("senha", "")
                    usuarioEncontrado = true
                    break
                }
            }

            if (!usuarioEncontrado) {
                Toast.makeText(requireContext(), "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // PRIMEIRA MENSAGEM: Valida se a senha atual digitada corresponde com a do JSON
            if (senhaAtualDigitada != senhaRealNoJson) {
                Toast.makeText(requireContext(), "A senha atual não é essa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // SEGUNDA MENSAGEM: Valida se a nova senha e a confirmação são iguais
            if (novaSenhaDigitada != confirmarSenhaDigitada) {
                Toast.makeText(requireContext(), "A senha nova e sua confirmação não correspondem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (novaSenhaDigitada.isEmpty()) {
                Toast.makeText(requireContext(), "A nova senha não pode estar vazia.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Se passou pelas validações, atualiza o JSON sem navegar para outra página
            val sucesso = jsonBD.atualizarSenhaUsuario(emailLogado, novaSenhaDigitada)

            if (sucesso) {
                Toast.makeText(requireContext(), "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                // Limpa os campos após salvar com sucesso
                binding.etSenhaAtual.text?.clear()
                binding.etNovaSenha.text?.clear()
                binding.etConfirmarSenha.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Erro ao atualizar a senha no armazenamento.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarVisibilidadeSenhas() {
        // Toggle Senha Atual
        binding.btnToggleSenhaAtual.setOnClickListener {
            senhaAtualVisivel = !senhaAtualVisivel
            if (senhaAtualVisivel) {
                binding.etSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.etSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etSenhaAtual.setSelection(binding.etSenhaAtual.text?.length ?: 0)
        }

        // Toggle Nova Senha
        binding.btnToggleNovaSenha.setOnClickListener {
            novaSenhaVisivel = !novaSenhaVisivel
            if (novaSenhaVisivel) {
                binding.etNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.etNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etNovaSenha.setSelection(binding.etNovaSenha.text?.length ?: 0)
        }

        // Toggle Confirmar Nova Senha
        binding.btnToggleConfirmarSenha.setOnClickListener {
            confirmarSenhaVisivel = !confirmarSenhaVisivel
            if (confirmarSenhaVisivel) {
                binding.etConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.etConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etConfirmarSenha.setSelection(binding.etConfirmarSenha.text?.length ?: 0)
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