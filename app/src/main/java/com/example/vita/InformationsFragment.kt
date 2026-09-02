package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentInformationsBinding

class InformationsFragment : Fragment() {

    private var _binding: FragmentInformationsBinding? = null
    private val binding get() = _binding!!

    // Injeção do ViewModel compartilhado
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura o Spinner
        configurarSpinnerSexo()

        // Seta de voltar
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botão Continuar
        binding.continueBtn.setOnClickListener {
            val peso = binding.edtPeso.text.toString().trim()
            val altura = binding.edtAltura.text.toString().trim()
            val pesoMeta = binding.edtPesoMeta.text.toString().trim()
            val sexo = binding.spinnerSexo.selectedItem.toString()
            val nasc = binding.edtNasc.text.toString().trim()

            if (peso.isEmpty() || altura.isEmpty() || pesoMeta.isEmpty() || nasc.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Grava as informações no ViewModel compartilhado
            userViewModel.peso = peso
            userViewModel.altura = altura
            userViewModel.pesoMeta = pesoMeta
            userViewModel.sexo = sexo
            userViewModel.nascimento = nasc

            // Avança para a tela de Nível de Atividade (WorkoutFragment)
            findNavController().navigate(R.id.action_informationsFragment_to_workoutFragment)
        }
    }

    private fun configurarSpinnerSexo() {
        val opcoesSexo = arrayOf("Masculino", "Feminino")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_sexo,
            opcoesSexo
        )
        adapter.setDropDownViewResource(R.layout.dropdown_sexo)

        binding.spinnerSexo.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}