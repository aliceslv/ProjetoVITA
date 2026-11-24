package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentStartBinding
import com.google.firebase.auth.FirebaseAuth

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null

    private val binding: FragmentStartBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se já existir uma sessão Firebase, abre a tela inicial.
        if (FirebaseAuth.getInstance().currentUser != null) {
            findNavController().navigate(R.id.inicioFragment)
            return
        }

        binding.entrarBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_startFragment_to_loginFragment
            )
        }

        binding.criarBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_startFragment_to_cadastroFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}