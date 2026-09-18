package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentProfileBinding
import com.example.vita.json.JsonBD
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacao()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega as informações do perfil sempre que a tela fica visível
        carregarDadosPerfil()
    }

    private fun carregarDadosPerfil() {
        val emailLogado = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(emailLogado, ignoreCase = true)) {

                // 1. Nome do Usuário
                val nome = user.optString("nome", "Usuário")
                binding.tvName.text = nome

                // 2. Peso Atual
                val peso = user.optString("peso", "0")
                binding.tvPesoValor.text = peso

                // 3. Foco (Dias desde a criação da conta)
                val userId = user.optString("id", "")
                val diasFoco = calcularDiasDeFoco(userId)
                binding.tvFocoValor.text = diasFoco.toString()

                break
            }
        }
    }

    private fun calcularDiasDeFoco(idUsuario: String): Long {
        return try {
            // O ID é gravado usando System.currentTimeMillis() durante o cadastro
            val criacaoMillis = idUsuario.toLong()
            val hojeMillis = System.currentTimeMillis()
            val diferencaMillis = hojeMillis - criacaoMillis

            // Converte a diferença de milissegundos para dias
            val dias = TimeUnit.MILLISECONDS.toDays(diferencaMillis)

            // Retorna no mínimo 1 para representar o primeiro dia de uso
            if (dias < 1) 1 else dias
        } catch (e: Exception) {
            1 // Valor padrão de fallback
        }
    }

    private fun configurarNavegacao() {
        // Botão Editar Perfil -> ProfileEditFragment
        binding.btnEditarPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        }

        // --- BARRA INFERIOR DE NAVEGAÇÃO ---

        // Ícone do Livro -> InicioFragment
        binding.btnLivro.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_inicioFragment)
        }

        // Ícone da Escala -> CaloriasFragment
        binding.btnEscala.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_caloriasFragment)
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