package com.example.vita

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentIdrBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class IdrFragment : Fragment() {

    private var _binding: FragmentIdrBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var dbManager: JsonBD
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        dbManager = JsonBD(requireContext())
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Calcula a IDR a partir dos dados do UserViewModel
        val idrFinal = calcularIDR(
            meta = userViewModel.meta.ifEmpty { "Manter peso" },
            pesoStr = userViewModel.peso.ifEmpty { "70" },
            alturaStr = userViewModel.altura.ifEmpty { "170" },
            sexo = userViewModel.sexo.ifEmpty { "Masculino" },
            nascStr = userViewModel.nascimento.ifEmpty { "01/01/2000" },
            nivelExercicio = userViewModel.nivelAtividade.ifEmpty { "Baixo" }
        )

        userViewModel.idrCalculado = idrFinal
        binding.txtValorCalorias.text = idrFinal.toString()

        // Seta de voltar
        binding.icarrow.setOnClickListener {
            findNavController().navigateUp()
        }

        // Concluir cadastro
        binding.idrbtn.setOnClickListener {
            if (userViewModel.email.isEmpty() || userViewModel.senha.isEmpty()) {
                Toast.makeText(requireContext(), "Dados de cadastro inválidos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cadastrarEGravarDados()
        }
    }

    private fun cadastrarEGravarDados() {
        val email = userViewModel.email
        val senha = userViewModel.senha

        // 1. Autenticação no Firebase
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: System.currentTimeMillis().toString()

                    // 2. Gravação no Firebase Firestore (apenas dados essenciais para auth/recuperação)
                    val usuarioMap = hashMapOf(
                        "nome" to userViewModel.nome,
                        "email" to email,
                        "meta" to userViewModel.meta,
                        "peso" to userViewModel.peso,
                        "altura" to userViewModel.altura,
                        "pesoMeta" to userViewModel.pesoMeta,
                        "sexo" to userViewModel.sexo,
                        "nascimento" to userViewModel.nascimento,
                        "nivelExercicio" to userViewModel.nivelAtividade,
                        "idr" to userViewModel.idrCalculado
                    )

                    db.collection("usuarios")
                        .document(userId)
                        .set(usuarioMap)

                    // 3. Salva os dados locais acumulados no arquivo JSON (JsonBD)
                    dbManager.salvarPerfilUsuario(
                        nome = userViewModel.nome,
                        email = email,
                        senha = senha,
                        meta = userViewModel.meta,
                        peso = userViewModel.peso,
                        altura = userViewModel.altura,
                        pesoMeta = userViewModel.pesoMeta,
                        sexo = userViewModel.sexo,
                        nascimento = userViewModel.nascimento,
                        nivelExercicio = userViewModel.nivelAtividade,
                        idr = userViewModel.idrCalculado
                    )

                    Toast.makeText(requireContext(), "Perfil cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

                    // 4. Redireciona para a StartActivity/MainActivity limpando a pilha de navegação
                    val intent = Intent(requireActivity(), StartFragment::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    val erro = task.exception?.message ?: "Erro ao cadastrar usuário no Firebase."
                    Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun calcularIDR(
        meta: String,
        pesoStr: String,
        alturaStr: String,
        sexo: String,
        nascStr: String,
        nivelExercicio: String
    ): Int {
        val peso = pesoStr.toDoubleOrNull() ?: 70.0
        val altura = alturaStr.toDoubleOrNull() ?: 170.0
        val idade = calcularIdade(nascStr)

        val tmb = if (sexo.equals("Masculino", ignoreCase = true)) {
            (10 * peso) + (6.25 * altura) - (5 * idade) + 5
        } else {
            (10 * peso) + (6.25 * altura) - (5 * idade) - 161
        }

        val fatorAtividade = when (nivelExercicio) {
            "Baixo" -> 1.2
            "Médio" -> 1.375
            "Alto" -> 1.55
            "Muito Alto" -> 1.725
            else -> 1.2
        }

        val gastoCaloricoTotal = tmb * fatorAtividade

        val idrFinal = when (meta) {
            "Emagrecimento" -> gastoCaloricoTotal - 400
            "Ganho de massa" -> gastoCaloricoTotal + 400
            else -> gastoCaloricoTotal
        }

        return idrFinal.toInt()
    }

    private fun calcularIdade(dataNascimento: String): Int {
        val partes = dataNascimento.split("/")
        if (partes.size != 3) return 25

        val dia = partes[0].toIntOrNull() ?: 1
        val mes = partes[1].toIntOrNull() ?: 1
        val ano = partes[2].toIntOrNull() ?: 2000

        val hoje = Calendar.getInstance()
        var idade = hoje.get(Calendar.YEAR) - ano

        if (hoje.get(Calendar.MONTH) + 1 < mes ||
            (hoje.get(Calendar.MONTH) + 1 == mes && hoje.get(Calendar.DAY_OF_MONTH) < dia)
        ) {
            idade--
        }

        return if (idade < 0) 25 else idade
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}