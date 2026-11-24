package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentAboutBinding // Import gerado automaticamente pelo View Binding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null

    private val binding: FragmentAboutBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflando o layout usando View Binding com base no XML enviado (fragment_about.xml)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navegação do botão voltar (btnBack) para a tela anterior (SettingsFragment)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpa a referência do binding para evitar vazamento de memória (Memory Leak)
        _binding = null
    }
}