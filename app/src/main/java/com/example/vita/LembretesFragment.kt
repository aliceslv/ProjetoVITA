package com.example.vita

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentLembretesBinding
import com.example.vita.ui.LembreteScheduler
import java.util.Locale

class LembretesFragment : Fragment() {

    private var _binding: FragmentLembretesBinding? = null
    private val binding get() = _binding!!

    private val ID_CAFE = 101
    private val ID_ALMOCO = 102
    private val ID_LANCHE = 103
    private val ID_JANTAR = 104
    private val ID_AGUA = 105
    private val ID_DORMIR = 106

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                "Permissão de notificação negada.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLembretesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefsGlobal = requireContext().getSharedPreferences("ConfiguracoesGlobaisPrefs", Context.MODE_PRIVATE)
        val notificacoesAtivadas = prefsGlobal.getBoolean("KEY_NOTIFICACOES_GLOBAIS", true)

        if (!notificacoesAtivadas) {
            // Se as notificações estiverem desativadas na privacidade, bloqueia a tela de lembretes
            bloquearTelaLembretes()
        } else {
            binding.txtAviso.visibility = View.GONE
            verificarPermissaoNotificacao()
            carregarConfiguracoes()
            configurarListeners()
        }
    }

    private fun bloquearTelaLembretes() {
        // Exibe o aviso na tela
        binding.txtAviso.visibility = View.VISIBLE

        // Desativa a interatividade dos cards de refeições/água/dormir
        binding.cardCafe.isEnabled = false
        binding.cardAlmoco.isEnabled = false
        binding.cardLanche.isEnabled = false
        binding.cardJantar.isEnabled = false
        binding.cardAgua.isEnabled = false
        binding.cardDormir.isEnabled = false

        // Desativa os switches individuais e muda a opacidade para indicar visualmente que estão desativados
        listOf(
            binding.switchCafe, binding.switchAlmoco, binding.switchLanche,
            binding.switchJantar, binding.switchAgua, binding.switchDormir
        ).forEach { switch ->
            switch.isEnabled = false
            switch.alpha = 0.5f
        }

        // Botão voltar continua funcionando normalmente
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun verificarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun carregarConfiguracoes() {
        val prefs = requireContext().getSharedPreferences("LembretesPrefs", Context.MODE_PRIVATE)

        binding.switchCafe.isChecked = prefs.getBoolean("KEY_CAFE", true)
        binding.switchAlmoco.isChecked = prefs.getBoolean("KEY_ALMOCO", true)
        binding.switchLanche.isChecked = prefs.getBoolean("KEY_LANCHE", false)
        binding.switchJantar.isChecked = prefs.getBoolean("KEY_JANTAR", true)
        binding.switchAgua.isChecked = prefs.getBoolean("KEY_AGUA", true)
        binding.switchDormir.isChecked = prefs.getBoolean("KEY_DORMIR", true)

        atualizarTextoCard(binding.txtHoraCafe, prefs.getInt("HORA_CAFE", 8), prefs.getInt("MIN_CAFE", 0), prefs.getString("DIAS_CAFE", "Todos os dias") ?: "Todos os dias")
        atualizarTextoCard(binding.txtHoraAlmoco, prefs.getInt("HORA_ALMOCO", 12), prefs.getInt("MIN_ALMOCO", 30), prefs.getString("DIAS_ALMOCO", "Seg a Sex") ?: "Seg a Sex")
        atualizarTextoCard(binding.txtHoraLanche, prefs.getInt("HORA_LANCHE", 16), prefs.getInt("MIN_LANCHE", 0), prefs.getString("DIAS_LANCHE", "Todos os dias") ?: "Todos os dias")
        atualizarTextoCard(binding.txtHoraJantar, prefs.getInt("HORA_JANTAR", 19), prefs.getInt("MIN_JANTAR", 30), prefs.getString("DIAS_JANTAR", "Todos os dias") ?: "Todos os dias")
        atualizarTextoCard(binding.txtHoraAgua, prefs.getInt("HORA_AGUA", 8), prefs.getInt("MIN_AGUA", 0), prefs.getString("DIAS_AGUA", "A cada 2h") ?: "A cada 2h")
        atualizarTextoCard(binding.txtHoraDormir, prefs.getInt("HORA_DORMIR", 22), prefs.getInt("MIN_DORMIR", 30), prefs.getString("DIAS_DORMIR", "Todos os dias") ?: "Todos os dias")
    }

    private fun atualizarTextoCard(textView: TextView, hora: Int, minuto: Int, frequencia: String) {
        val horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", hora, minuto)
        textView.text = "$horaFormatada • $frequencia"
    }

    private fun salvarEstadoSwitch(key: String, isChecked: Boolean) {
        val prefs = requireContext().getSharedPreferences("LembretesPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, isChecked).apply()
    }

    private fun abrirDialogoEdicao(
        id: Int, keyHora: String, keyMin: String, keyDias: String,
        horaPadrao: Int, minPadrao: Int, diasPadrao: String,
        titulo: String, mensagem: String, textView: TextView, isChecked: Boolean
    ) {
        val prefs = requireContext().getSharedPreferences("LembretesPrefs", Context.MODE_PRIVATE)
        var horaSelecionada = prefs.getInt(keyHora, horaPadrao)
        var minSelecionado = prefs.getInt(keyMin, minPadrao)

        val opcoesDias = arrayOf("Todos os dias", "Seg a Sex", "Fins de semana")
        val diaAtualSalvo = prefs.getString(keyDias, diasPadrao) ?: diasPadrao
        var indiceSelecionado = opcoesDias.indexOf(diaAtualSalvo).coerceAtLeast(0)

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                horaSelecionada = hourOfDay
                minSelecionado = minute

                AlertDialog.Builder(requireContext())
                    .setTitle("Repetir em quais dias?")
                    .setSingleChoiceItems(opcoesDias, indiceSelecionado) { _, which ->
                        indiceSelecionado = which
                    }
                    .setPositiveButton("Salvar") { _, _ ->
                        val diasSelecionados = opcoesDias[indiceSelecionado]
                        prefs.edit()
                            .putInt(keyHora, horaSelecionada)
                            .putInt(keyMin, minSelecionado)
                            .putString(keyDias, diasSelecionados)
                            .apply()

                        atualizarTextoCard(textView, horaSelecionada, minSelecionado, diasSelecionados)

                        if (isChecked) {
                            agendarSeguro(id, horaSelecionada, minSelecionado, titulo, mensagem)
                        }
                        Toast.makeText(requireContext(), "Lembrete configurado!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            horaSelecionada, minSelecionado, true
        ).show()
    }

    private fun agendarSeguro(id: Int, hora: Int, minuto: Int, titulo: String, mensagem: String) {
        try {
            LembreteScheduler.agendarLembrete(requireContext(), id, hora, minuto, titulo, mensagem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelarSeguro(id: Int) {
        try {
            LembreteScheduler.cancelarLembrete(requireContext(), id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun configurarListeners() {
        binding.btnVoltar.setOnClickListener {
            findNavController().navigateUp()
        }

        val prefs = requireContext().getSharedPreferences("LembretesPrefs", Context.MODE_PRIVATE)

        // Café
        binding.cardCafe.setOnClickListener {
            abrirDialogoEdicao(ID_CAFE, "HORA_CAFE", "MIN_CAFE", "DIAS_CAFE", 8, 0, "Todos os dias", "Café da Manhã ☕", "Hora de tomar o seu café da manhã!", binding.txtHoraCafe, binding.switchCafe.isChecked)
        }
        binding.switchCafe.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_CAFE", isChecked)
            if (isChecked) agendarSeguro(ID_CAFE, prefs.getInt("HORA_CAFE", 8), prefs.getInt("MIN_CAFE", 0), "Café da Manhã ☕", "Hora de tomar o seu café da manhã!")
            else cancelarSeguro(ID_CAFE)
        }

        // Almoço
        binding.cardAlmoco.setOnClickListener {
            abrirDialogoEdicao(ID_ALMOCO, "HORA_ALMOCO", "MIN_ALMOCO", "DIAS_ALMOCO", 12, 30, "Seg a Sex", "Almoço 🍲", "Está na hora do seu almoço!", binding.txtHoraAlmoco, binding.switchAlmoco.isChecked)
        }
        binding.switchAlmoco.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_ALMOCO", isChecked)
            if (isChecked) agendarSeguro(ID_ALMOCO, prefs.getInt("HORA_ALMOCO", 12), prefs.getInt("MIN_ALMOCO", 30), "Almoço 🍲", "Está na hora do seu almoço!")
            else cancelarSeguro(ID_ALMOCO)
        }

        // Lanche
        binding.cardLanche.setOnClickListener {
            abrirDialogoEdicao(ID_LANCHE, "HORA_LANCHE", "MIN_LANCHE", "DIAS_LANCHE", 16, 0, "Todos os dias", "Lanche da Tarde 🍎", "Hora da pausa para o lanche!", binding.txtHoraLanche, binding.switchLanche.isChecked)
        }
        binding.switchLanche.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_LANCHE", isChecked)
            if (isChecked) agendarSeguro(ID_LANCHE, prefs.getInt("HORA_LANCHE", 16), prefs.getInt("MIN_LANCHE", 0), "Lanche da Tarde 🍎", "Hora da pausa para o lanche!")
            else cancelarSeguro(ID_LANCHE)
        }

        // Jantar
        binding.cardJantar.setOnClickListener {
            abrirDialogoEdicao(ID_JANTAR, "HORA_JANTAR", "MIN_JANTAR", "DIAS_JANTAR", 19, 30, "Todos os dias", "Jantar 🥗", "Hora do jantar!", binding.txtHoraJantar, binding.switchJantar.isChecked)
        }
        binding.switchJantar.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_JANTAR", isChecked)
            if (isChecked) agendarSeguro(ID_JANTAR, prefs.getInt("HORA_JANTAR", 19), prefs.getInt("MIN_JANTAR", 30), "Jantar 🥗", "Hora do jantar!")
            else cancelarSeguro(ID_JANTAR)
        }

        // Água
        binding.cardAgua.setOnClickListener {
            abrirDialogoEdicao(ID_AGUA, "HORA_AGUA", "MIN_AGUA", "DIAS_AGUA", 8, 0, "A cada 2h", "Beber Água 💧", "Mantenha-se hidratado!", binding.txtHoraAgua, binding.switchAgua.isChecked)
        }
        binding.switchAgua.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_AGUA", isChecked)
            if (isChecked) agendarSeguro(ID_AGUA, prefs.getInt("HORA_AGUA", 8), prefs.getInt("MIN_AGUA", 0), "Beber Água 💧", "Mantenha-se hidratado!")
            else cancelarSeguro(ID_AGUA)
        }

        // Dormir
        binding.cardDormir.setOnClickListener {
            abrirDialogoEdicao(ID_DORMIR, "HORA_DORMIR", "MIN_DORMIR", "DIAS_DORMIR", 22, 30, "Todos os dias", "Hora de Dormir 😴", "Boa noite!", binding.txtHoraDormir, binding.switchDormir.isChecked)
        }
        binding.switchDormir.setOnCheckedChangeListener { _, isChecked ->
            salvarEstadoSwitch("KEY_DORMIR", isChecked)
            if (isChecked) agendarSeguro(ID_DORMIR, prefs.getInt("HORA_DORMIR", 22), prefs.getInt("MIN_DORMIR", 30), "Hora de Dormir 😴", "Boa noite!")
            else cancelarSeguro(ID_DORMIR)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}