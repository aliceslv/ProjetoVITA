package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentPrivacidadeBinding

class PrivacidadeFragment : Fragment() {

    private var _binding: FragmentPrivacidadeBinding? = null

    private val binding: FragmentPrivacidadeBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacidadeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("ConfiguracoesGlobaisPrefs", Context.MODE_PRIVATE)

        // Carrega o estado atual do switch (padrão é ligado/true)
        val notificacoesAtivadas = prefs.getBoolean("KEY_NOTIFICACOES_GLOBAIS", true)
        binding.switchNotificacoes.isChecked = notificacoesAtivadas

        // Botão Voltar para o SettingsFragment
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        //Botão Ir para PasswordFragment
        binding.cardSenha.setOnClickListener {
            findNavController().navigate(R.id.action_privacidadeFragment_to_passwordFragment)
        }

        // Listener do Switch de Notificações Push
        binding.switchNotificacoes.setOnCheckedChangeListener { _, isChecked ->
            // Salva a preferência global
            prefs.edit().putBoolean("KEY_NOTIFICACOES_GLOBAIS", isChecked).apply()

            // Se o usuário desativou, podemos opcionalmente cancelar todos os alarmes do sistema aqui se desejar
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}