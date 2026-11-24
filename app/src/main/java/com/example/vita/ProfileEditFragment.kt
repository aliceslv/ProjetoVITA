package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.vita.ui.InputUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.data.repository.UsuarioRepository
import com.example.vita.databinding.FragmentProfileEditBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null

    private val binding: FragmentProfileEditBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val usuarioRepository = UsuarioRepository()

    private val opcoesMeta = listOf("Emagrecimento", "Manter peso", "Ganho de massa")
    private val opcoesNivelExercicio = listOf("Baixo", "Médio", "Alto", "Muito Alto")

    // Variáveis locais para reter dados fixos do usuário necessários para o cálculo do IDR
    private var sexoUsuario: String = "Masculino"
    private var nascimentoUsuario: String = ""

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

        InputUtils.configurarTeclado(view)

        configurarSpinners()
        carregarDadosPerfil()
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

    private fun carregarDadosPerfil() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        usuarioRepository.buscarPerfil(
            uid = uid,
            resultado = { dados ->
                if (!isAdded || _binding == null) return@buscarPerfil

                if (dados == null) {
                    Toast.makeText(requireContext(), "Perfil não encontrado.", Toast.LENGTH_SHORT).show()
                    return@buscarPerfil
                }

                // Armazena dados essenciais para cálculos futuros
                sexoUsuario = dados["sexo"]?.toString() ?: "Masculino"
                nascimentoUsuario = dados["nascimento"]?.toString() ?: ""

                val peso = dados["peso"]?.toString() ?: ""
                val altura = dados["altura"]?.toString() ?: ""
                val pesoMeta = dados["pesoMeta"]?.toString() ?: ""
                val meta = dados["meta"]?.toString() ?: ""
                val nivelExercicio = dados["nivelExercicio"]?.toString() ?: ""

                binding.inputPesoAtual.setText(peso)
                binding.inputAltura.setText(altura)
                binding.inputPesoMeta.setText(pesoMeta)

                val idxMeta = opcoesMeta.indexOfFirst { it.equals(meta, ignoreCase = true) }
                if (idxMeta >= 0) {
                    binding.spinnerMeta.setSelection(idxMeta)
                }

                val idxExercicio = opcoesNivelExercicio.indexOfFirst { it.equals(nivelExercicio, ignoreCase = true) }
                if (idxExercicio >= 0) {
                    binding.spinnerNivelExercicio.setSelection(idxExercicio)
                }

                // Atualiza o IMC na interface com os dados carregados
                calcularEExibirImc(peso, altura)
            },
            erro = { erro ->
                if (!isAdded || _binding == null) return@buscarPerfil
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar perfil: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
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
                imc < 18.5 -> Pair("Abaixo do peso", R.color.orange2)
                imc < 25.0 -> Pair("Peso normal", R.color.green1)
                imc < 30.0 -> Pair("Sobrepeso", R.color.orange2)
                imc < 35.0 -> Pair("Obesidade I", R.color.orange2)
                imc < 40.0 -> Pair("Obesidade II", R.color.orange2)
                else -> Pair("Obesidade III", R.color.orange2)
            }

            binding.txtStatusImc.text = status
            binding.txtStatusImc.setTextColor(requireContext().getColor(corResId))
        } else {
            binding.txtValorImc.text = "--"
            binding.txtStatusImc.text = "Sem dados"
        }
    }

    private fun salvarNovasMedidas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val novoPesoStr = binding.inputPesoAtual.text.toString().trim()
        val novaAlturaStr = binding.inputAltura.text.toString().trim()
        val novoPesoMetaStr = binding.inputPesoMeta.text.toString().trim()
        val novaMeta = binding.spinnerMeta.selectedItem?.toString() ?: ""
        val novoNivelExercicio = binding.spinnerNivelExercicio.selectedItem?.toString() ?: ""

        if (novoPesoStr.isEmpty() || novaAlturaStr.isEmpty() || novoPesoMetaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val pesoDouble = novoPesoStr.replace(",", ".").toDoubleOrNull()
        val alturaDouble = novaAlturaStr.replace(",", ".").toDoubleOrNull()
        val pesoMetaDouble = novoPesoMetaStr.replace(",", ".").toDoubleOrNull()

        if (pesoDouble == null || alturaDouble == null || pesoMetaDouble == null || pesoDouble <= 0 || alturaDouble <= 0) {
            Toast.makeText(requireContext(), "Informe valores válidos para peso e altura.", Toast.LENGTH_SHORT).show()
            return
        }

        // Realiza o cálculo do novo IDR com base nos dados atualizados + dados fixos (sexo e nascimento)
        val novoIdr = calcularIdr(
            peso = pesoDouble,
            altura = if (alturaDouble > 3.0) alturaDouble / 100.0 else alturaDouble, // Garante altura em metros para TMB se necessário
            nascimento = nascimentoUsuario,
            sexo = sexoUsuario,
            nivelAtividade = novoNivelExercicio,
            meta = novaMeta
        )

        val novaMetaAgua = (pesoDouble * 35).toInt()

        // Prepara o mapa de dados a serem enviados para o Firestore
        val dadosAtualizados = hashMapOf<String, Any>(
            "peso" to pesoDouble,
            "altura" to alturaDouble,
            "pesoMeta" to pesoMetaDouble,
            "meta" to novaMeta,
            "nivelExercicio" to novoNivelExercicio,
            "idr" to novoIdr, // Salva o novo IDR recalculado para consumo no Inicio Fragment
            "metaAgua" to novaMetaAgua, // Salva nova meta de água recalculada
            "updatedAt" to Timestamp.now()
        )

        binding.btnSalvarMedidas.isEnabled = false

        usuarioRepository.atualizarPerfil(
            uid = uid,
            dados = dadosAtualizados,
            sucesso = {
                if (!isAdded || _binding == null) return@atualizarPerfil

                binding.btnSalvarMedidas.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Dados, IDR e IMC atualizados com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                // Atualiza visualmente o IMC na própria tela de imediato
                calcularEExibirImc(novoPesoStr, novaAlturaStr)
            },
            erro = { erro ->
                if (!isAdded || _binding == null) return@atualizarPerfil

                binding.btnSalvarMedidas.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Erro ao atualizar dados: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
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

        // Se a altura estiver em centímetros (> 3.0), converte para metros para a fórmula de Harris-Benedict correta (ou ajuste se sua fórmula espera cm)
        val alturaEmCm = if (altura < 3.0) altura * 100.0 else altura

        val tmb = if (sexo.equals("Feminino", ignoreCase = true)) {
            (10.0 * peso) + (6.25 * alturaEmCm) - (5.0 * idade) - 161.0
        } else {
            (10.0 * peso) + (6.25 * alturaEmCm) - (5.0 * idade) + 5.0
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}