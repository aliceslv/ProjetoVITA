package com.example.vita

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentProfileEditBinding
import com.example.vita.json.JsonBD
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val opcoesMeta = listOf("Emagrecimento", "Manter peso", "Ganho de massa")
    private val opcoesNivelExercicio = listOf("Baixo", "Médio", "Alto", "Muito Alto")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSpinners()
        carregarDadosEAtualizarImc()
        configurarAcoes()
    }

    private fun configurarSpinners() {
        val adapterMeta = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_objetivo,
            opcoesMeta
        )
        adapterMeta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMeta.adapter = adapterMeta

        val adapterExercicio = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_objetivo,
            opcoesNivelExercicio
        )
        adapterExercicio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNivelExercicio.adapter = adapterExercicio
    }

    private fun carregarDadosEAtualizarImc() {
        val emailLogado = obterEmailUsuarioLogado()
        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(emailLogado, ignoreCase = true)) {

                val pesoStr = user.optString("peso", "0")
                val alturaStr = user.optString("altura", "0")
                val pesoMetaStr = user.optString("pesoMeta", "0")
                val metaStr = user.optString("meta", "")
                val nivelExercicioStr = user.optString("nivelExercicio", "")

                binding.inputPesoAtual.setText(pesoStr)
                binding.inputAltura.setText(alturaStr)
                binding.inputPesoMeta.setText(pesoMetaStr)

                val idxMeta = opcoesMeta.indexOfFirst { it.equals(metaStr, ignoreCase = true) }
                if (idxMeta >= 0) {
                    binding.spinnerMeta.setSelection(idxMeta)
                }

                val idxExercicio = opcoesNivelExercicio.indexOfFirst { it.equals(nivelExercicioStr, ignoreCase = true) }
                if (idxExercicio >= 0) {
                    binding.spinnerNivelExercicio.setSelection(idxExercicio)
                }

                calcularEExibirImc(pesoStr, alturaStr)
                break
            }
        }
    }

    private fun calcularEExibirImc(pesoStr: String, alturaStr: String) {
        val peso = pesoStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        var altura = alturaStr.replace(",", ".").toDoubleOrNull() ?: 0.0

        if (peso > 0.0 && altura > 0.0) {
            if (altura > 3.0) {
                altura /= 100.0
            }

            val imc = peso / (altura * altura)
            binding.txtValorImc.text = String.format(Locale.US, "%.1f", imc)

            val (status, corResId) = when {
                imc < 18.5 -> Pair("Abaixo do peso", R.color.orange)
                imc < 25.0 -> Pair("Peso normal", R.color.green1)
                imc < 30.0 -> Pair("Sobrepeso", R.color.orange)
                imc < 35.0 -> Pair("Obesidade I", R.color.orange)
                imc < 40.0 -> Pair("Obesidade II", R.color.orange)
                else -> Pair("Obesidade III", R.color.orange)
            }

            binding.txtStatusImc.text = status
            binding.txtStatusImc.setTextColor(requireContext().getColor(corResId))
        } else {
            binding.txtValorImc.text = "--"
            binding.txtStatusImc.text = "Sem dados"
        }
    }

    private fun salvarNovasMedidas() {
        val emailLogado = obterEmailUsuarioLogado()
        val novoPeso = binding.inputPesoAtual.text.toString().trim()
        val novaAltura = binding.inputAltura.text.toString().trim()
        val novoPesoMeta = binding.inputPesoMeta.text.toString().trim()
        val novaMeta = binding.spinnerMeta.selectedItem?.toString() ?: ""
        val novoNivelExercicio = binding.spinnerNivelExercicio.selectedItem?.toString() ?: ""

        if (novoPeso.isEmpty() || novaAltura.isEmpty() || novoPesoMeta.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBD = JsonBD(requireContext())
        val users = jsonBD.getUsers()

        var sexo = "Masculino"
        var nascimento = ""

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(emailLogado, ignoreCase = true)) {
                sexo = user.optString("sexo", "Masculino")
                nascimento = user.optString("nascimento", "")
                break
            }
        }

        val pesoDouble = novoPeso.replace(",", ".").toDoubleOrNull() ?: 0.0

        val novoIdr = calcularIdr(
            peso = pesoDouble,
            altura = novaAltura.toDoubleOrNull() ?: 0.0,
            nascimento = nascimento,
            sexo = sexo,
            nivelAtividade = novoNivelExercicio,
            meta = novaMeta
        )

        val sucesso = jsonBD.atualizarMedidasUsuario(
            email = emailLogado,
            novoPeso = novoPeso,
            novaAltura = novaAltura,
            novoPesoMeta = novoPesoMeta,
            novaMeta = novaMeta,
            novoNivelExercicio = novoNivelExercicio,
            novoIdr = novoIdr
        )

        if (sucesso) {
            Toast.makeText(requireContext(), "Dados, IDR e Meta de Água atualizados!", Toast.LENGTH_SHORT).show()
            carregarDadosEAtualizarImc()
        } else {
            Toast.makeText(requireContext(), "Erro ao atualizar dados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calcularIdr(
        peso: Double,
        altura: Double,
        nascimento: String,
        sexo: String,
        nivelAtividade: String,
        meta: String
    ): Int {
        val idade = calcularIdade(nascimento)

        val tmb = if (sexo.equals("Feminino", ignoreCase = true)) {
            (10.0 * peso) + (6.25 * altura) - (5.0 * idade) - 161.0
        } else {
            (10.0 * peso) + (6.25 * altura) - (5.0 * idade) + 5.0
        }

        val fatorAtividade = when (nivelAtividade) {
            "Baixo" -> 1.2
            "Médio" -> 1.375
            "Alto" -> 1.55
            "Muito Alto" -> 1.725
            else -> 1.2
        }

        val getGastoTotal = tmb * fatorAtividade

        val idrFinal = when (meta) {
            "Emagrecimento" -> getGastoTotal - 500.0
            "Ganho de massa" -> getGastoTotal + 400.0
            "Manter peso" -> getGastoTotal
            else -> getGastoTotal
        }

        return idrFinal.toInt().coerceAtLeast(1200)
    }

    private fun calcularIdade(dataNascimento: String): Int {
        return try {
            val sdf = if (dataNascimento.contains("-")) {
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            } else {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            }
            val date = sdf.parse(dataNascimento) ?: return 25
            val dob = Calendar.getInstance().apply { time = date }
            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age.coerceAtLeast(10)
        } catch (e: Exception) {
            25
        }
    }

    private fun configurarAcoes() {
        binding.icFechar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSalvarMedidas.setOnClickListener {
            salvarNovasMedidas()
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