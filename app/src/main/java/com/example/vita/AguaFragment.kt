package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentAguaBinding
import com.example.vita.json.JsonBD
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AguaFragment : Fragment() {

    private var _binding: FragmentAguaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAguaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())
        val (peso, metaAguaMl) = obterPesoEMetaUsuario(jsonBD, email)

        binding.tvMetaCalculada.text = "$metaAguaMl ml (baseada no seu peso de ${peso.toInt()}kg)"

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.root.findViewById<View?>(R.id.btnCancelar)?.setOnClickListener { findNavController().navigateUp() }

        binding.btnSalvarAgua.setOnClickListener {
            val qtdStr = binding.etQuantidadeMl.text.toString().trim()
            val qtdMl = qtdStr.toIntOrNull()

            if (qtdMl == null || qtdMl <= 0) {
                Toast.makeText(requireContext(), "Informe uma quantidade válida!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hojeStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val sucesso = jsonBD.salvarRegistroAgua(email, qtdMl, hojeStr)
            if (sucesso) {
                Toast.makeText(requireContext(), "Registro de água salvo!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Erro ao salvar registro.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obterEmailUsuarioLogado(): String {
        val sharedPref = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_EMAIL", "") ?: ""
    }

    private fun obterPesoEMetaUsuario(jsonBD: JsonBD, email: String): Pair<Double, Int> {
        val users = jsonBD.getUsers()
        for (i in 0 until users.length()) {
            val u = users.getJSONObject(i)
            if (u.optString("email").equals(email, ignoreCase = true)) {
                val peso = u.optString("peso", "70").replace(",", ".").toDoubleOrNull() ?: 70.0
                val meta = if (u.has("metaAgua")) {
                    u.optInt("metaAgua", (peso * 35).toInt())
                } else {
                    (peso * 35).toInt()
                }
                return Pair(peso, meta)
            }
        }
        return Pair(70.0, 2450)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}