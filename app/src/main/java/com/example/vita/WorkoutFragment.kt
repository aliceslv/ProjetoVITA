package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentWorkoutBinding

class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    // Resgata o ViewModel compartilhado
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botão voltar
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        // Configuração dos 4 níveis
        binding.btnBaixo.setOnClickListener {
            navegarParaIdr("Baixo")
        }

        binding.btnMedio.setOnClickListener {
            navegarParaIdr("Médio")
        }

        binding.btnAlto.setOnClickListener {
            navegarParaIdr("Alto")
        }

        binding.btnMuitoAlto.setOnClickListener {
            navegarParaIdr("Muito Alto")
        }
    }

    private fun navegarParaIdr(nivelExercicio: String) {
        // Guarda a informação de nível de exercício no ViewModel
        userViewModel.nivelAtividade = nivelExercicio

        // Avança para o Fragment final do cálculo IDR e salvamento dos dados
        findNavController().navigate(R.id.action_workoutFragment_to_idrFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}