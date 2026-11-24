package com.example.vita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.data.FirestorePaths
import com.example.vita.databinding.FragmentIdrBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IdrFragment : Fragment() {

    private var _binding: FragmentIdrBinding? = null

    private val binding: FragmentIdrBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

    private val userViewModel: UserViewModel by activityViewModels()

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idr = calcularIDR(
            userViewModel.meta.ifEmpty { "Manter peso" },
            userViewModel.peso.ifEmpty { "70" },
            userViewModel.altura.ifEmpty { "170" },
            userViewModel.sexo.ifEmpty { "Masculino" },
            userViewModel.nascimento.ifEmpty { "01/01/2000" },
            userViewModel.nivelAtividade.ifEmpty { "Baixo" }
        )

        userViewModel.idrCalculado = idr

        binding.txtValorCalorias.text = idr.toString()

        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.idrbtn.setOnClickListener {

            if (
                userViewModel.nome.isBlank() ||
                userViewModel.email.isBlank() ||
                userViewModel.senha.isBlank()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Dados de cadastro inválidos.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            cadastrarEGravarDados()
        }
    }

    private fun cadastrarEGravarDados() {

        binding.idrbtn.isEnabled = false

        val peso = userViewModel.peso
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        val altura = userViewModel.altura
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        criarContaFirebase(peso, altura)
    }

    private fun criarContaFirebase(
        peso: Double,
        altura: Double
    ) {

        auth.createUserWithEmailAndPassword(
            userViewModel.email,
            userViewModel.senha
        ).addOnCompleteListener { tarefa ->

            if (!isAdded) return@addOnCompleteListener

            if (!tarefa.isSuccessful) {
                tratarErroCadastro(
                    tarefa.exception?.localizedMessage
                        ?: "Erro ao criar conta."
                )
                return@addOnCompleteListener
            }

            val uid = auth.currentUser?.uid

            if (uid == null) {
                tratarErroCadastro(
                    "Não foi possível identificar a conta criada."
                )
                return@addOnCompleteListener
            }

            salvarPerfilFirestore(uid, peso, altura)
        }
    }

    private fun criarDadosPerfil(
        peso: Double,
        altura: Double
    ): HashMap<String, Any> {

        val metaAgua = (peso * 35).toInt()

        return hashMapOf(
            "nome" to userViewModel.nome,
            "email" to userViewModel.email.trim().lowercase(Locale.ROOT),
            "meta" to userViewModel.meta,
            "peso" to peso,
            "altura" to altura,
            "pesoMeta" to userViewModel.pesoMeta,
            "sexo" to userViewModel.sexo,
            "nascimento" to userViewModel.nascimento,
            "nivelExercicio" to userViewModel.nivelAtividade,
            "idr" to userViewModel.idrCalculado,
            "metaAgua" to metaAgua,
            "createdAt" to Timestamp(Date())
        )
    }

    private fun salvarPerfilFirestore(
        uid: String,
        peso: Double,
        altura: Double
    ) {

        val dados = criarDadosPerfil(peso, altura)

        db.collection(FirestorePaths.USERS)
            .document(uid)
            .set(dados)
            .addOnSuccessListener {

                if (!isAdded) return@addOnSuccessListener

                binding.idrbtn.isEnabled = true
                userViewModel.senha = ""

                Toast.makeText(
                    requireContext(),
                    "Perfil cadastrado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_idrFragment_to_inicioFragment
                )
            }
            .addOnFailureListener { erro ->

                if (!isAdded) return@addOnFailureListener

                auth.currentUser?.delete()

                tratarErroCadastro(
                    "Erro ao salvar perfil: ${erro.localizedMessage}"
                )
            }
    }

    private fun tratarErroCadastro(mensagem: String) {

        binding.idrbtn.isEnabled = true

        Toast.makeText(
            requireContext(),
            mensagem,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun calcularIDR(
        meta: String,
        pesoStr: String,
        alturaStr: String,
        sexo: String,
        nascStr: String,
        nivelExercicio: String
    ): Int {

        val peso = pesoStr.replace(",", ".").toDoubleOrNull() ?: 70.0
        val altura = alturaStr.replace(",", ".").toDoubleOrNull() ?: 170.0

        val idade = calcularIdade(nascStr)

        val tmb = if (sexo.equals("Masculino", ignoreCase = true)) {
            (10.0 * peso) +
                    (6.25 * altura) -
                    (5.0 * idade) + 5.0
        } else {
            (10.0 * peso) +
                    (6.25 * altura) -
                    (5.0 * idade) - 161.0
        }

        val fatorAtividade = when (nivelExercicio) {
            "Baixo" -> 1.2
            "Médio" -> 1.375
            "Alto" -> 1.55
            "Muito Alto" -> 1.725
            else -> 1.2
        }

        val gastoCaloricoTotal = tmb * fatorAtividade

        val idrFinal = when {
            meta.contains("Emagrecimento", ignoreCase = true) ||
                    meta.contains("Emagrecer", ignoreCase = true) ->
                gastoCaloricoTotal - 500.0

            meta.contains("Ganho", ignoreCase = true) ||
                    meta.contains("Massa", ignoreCase = true) ->
                gastoCaloricoTotal + 400.0

            else -> gastoCaloricoTotal
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

            val dob = Calendar.getInstance().apply {
                time = date
            }

            val hoje = Calendar.getInstance()

            var idade = hoje.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

            if (
                hoje.get(Calendar.DAY_OF_YEAR) <
                dob.get(Calendar.DAY_OF_YEAR)
            ) {
                idade--
            }

            idade.coerceAtLeast(10)

        } catch (e: Exception) {
            25
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}