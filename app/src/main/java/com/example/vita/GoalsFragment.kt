package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentGoalsBinding

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    // Resgata a mesma instância do UserViewModel
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botão para voltar ao fragment anterior
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        // Opção 1: Emagrecimento
        binding.btnweightloss.setOnClickListener {
            navegarParaInformations("Emagrecimento")
        }

        // Opção 2: Manter Peso
        binding.btnKW.setOnClickListener {
            navegarParaInformations("Manter peso")
        }

        // Opção 3: Ganho de Massa
        binding.btnGW.setOnClickListener {
            navegarParaInformations("Ganho de massa")
        }
    }

    private fun navegarParaInformations(metaSelecionada: String) {
        // Salva a meta escolhida no ViewModel compartilhado
        userViewModel.meta = metaSelecionada

        // Avança para a próxima tela do onboarding (InformationsFragment)
        findNavController().navigate(R.id.action_goalsFragment_to_informationsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}