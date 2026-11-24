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
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentLembretesBinding
import com.example.vita.ui.LembreteScheduler
import java.util.Locale

class LembretesFragment : Fragment() {

    private var _binding: FragmentLembretesBinding? = null

    private val binding: FragmentLembretesBinding
        get() = checkNotNull(_binding) {
            "Binding acessado fora do ciclo de vida da View."
        }

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

        val prefsGlobal =
            requireContext().getSharedPreferences("ConfiguracoesGlobaisPrefs", Context.MODE_PRIVATE)
        val notificacoesAtivadas = prefsGlobal.getBoolean("KEY_NOTIFICACOES_GLOBAIS", true)

        if (!notificacoesAtivadas) {
            // Se as notificações estiverem desativadas na privacidade, bloqueia a tela de lembretes
            bloquearTelaLembretes()
        } else {
            binding.txtAviso.visibility = View.GONE
            verificarPermissaoNotificacao()
            carregarConfiguracoes()
            configurarListeners()

            if (binding.switchAgua.isChecked) {
                LembreteScheduler.agendarProximoLembreteAgua(
                    requireContext()
                )
            }
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

        atualizarTextoCard(
            binding.txtHoraCafe,
            prefs.getInt("HORA_CAFE", 8),
            prefs.getInt("MIN_CAFE", 0),
            prefs.getString("DIAS_CAFE", "Todos os dias") ?: "Todos os dias"
        )
        atualizarTextoCard(
            binding.txtHoraAlmoco,
            prefs.getInt("HORA_ALMOCO", 12),
            prefs.getInt("MIN_ALMOCO", 30),
            prefs.getString("DIAS_ALMOCO", "Seg a Sex") ?: "Seg a Sex"
        )
        atualizarTextoCard(
            binding.txtHoraLanche,
            prefs.getInt("HORA_LANCHE", 16),
            prefs.getInt("MIN_LANCHE", 0),
            prefs.getString("DIAS_LANCHE", "Todos os dias") ?: "Todos os dias"
        )
        atualizarTextoCard(
            binding.txtHoraJantar,
            prefs.getInt("HORA_JANTAR", 19),
            prefs.getInt("MIN_JANTAR", 30),
            prefs.getString("DIAS_JANTAR", "Todos os dias") ?: "Todos os dias"
        )
        atualizarTextoAgua(prefs)
        atualizarTextoCard(
            binding.txtHoraDormir,
            prefs.getInt("HORA_DORMIR", 22),
            prefs.getInt("MIN_DORMIR", 30),
            prefs.getString("DIAS_DORMIR", "Todos os dias") ?: "Todos os dias"
        )
    }

    private fun atualizarTextoAgua(
        prefs: android.content.SharedPreferences
    ) {

        val inicio = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            prefs.getInt("HORA_AGUA_INICIO", 8),
            prefs.getInt("MIN_AGUA_INICIO", 0)
        )

        val fim = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            prefs.getInt("HORA_AGUA_FIM", 22),
            prefs.getInt("MIN_AGUA_FIM", 0)
        )

        val intervalo = prefs.getInt("INTERVALO_AGUA", 2)

        val dias = prefs.getString(
            "DIAS_AGUA",
            "Todos os dias"
        ) ?: "Todos os dias"

        binding.txtHoraAgua.text =
            "$inicio às $fim • A cada ${intervalo}h • $dias"
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

                        atualizarTextoCard(
                            textView,
                            horaSelecionada,
                            minSelecionado,
                            diasSelecionados
                        )

                        if (isChecked) {
                            agendarSeguro(id, horaSelecionada, minSelecionado, titulo, mensagem)
                        }
                        Toast.makeText(
                            requireContext(),
                            "Lembrete configurado!",
                            Toast.LENGTH_SHORT
                        ).show()
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

        val prefs = requireContext().getSharedPreferences(
            "LembretesPrefs",
            Context.MODE_PRIVATE
        )

        configurarLembrete(
            binding.cardCafe,
            binding.switchCafe,
            binding.txtHoraCafe,
            ID_CAFE,
            "HORA_CAFE",
            "MIN_CAFE",
            "DIAS_CAFE",
            "KEY_CAFE",
            8,
            0,
            "Todos os dias",
            "Café da Manhã ☕",
            "Hora de tomar o seu café da manhã!",
            prefs
        )

        configurarLembrete(
            binding.cardAlmoco,
            binding.switchAlmoco,
            binding.txtHoraAlmoco,
            ID_ALMOCO,
            "HORA_ALMOCO",
            "MIN_ALMOCO",
            "DIAS_ALMOCO",
            "KEY_ALMOCO",
            12,
            30,
            "Seg a Sex",
            "Almoço 🍲",
            "Está na hora do seu almoço!",
            prefs
        )

        configurarLembrete(
            binding.cardLanche,
            binding.switchLanche,
            binding.txtHoraLanche,
            ID_LANCHE,
            "HORA_LANCHE",
            "MIN_LANCHE",
            "DIAS_LANCHE",
            "KEY_LANCHE",
            16,
            0,
            "Todos os dias",
            "Lanche da Tarde 🍎",
            "Hora da pausa para o lanche!",
            prefs
        )

        configurarLembrete(
            binding.cardJantar,
            binding.switchJantar,
            binding.txtHoraJantar,
            ID_JANTAR,
            "HORA_JANTAR",
            "MIN_JANTAR",
            "DIAS_JANTAR",
            "KEY_JANTAR",
            19,
            30,
            "Todos os dias",
            "Jantar 🥗",
            "Hora do jantar!",
            prefs
        )

        configurarLembrete(
            binding.cardAgua,
            binding.switchAgua,
            binding.txtHoraAgua,
            ID_AGUA,
            "HORA_AGUA",
            "MIN_AGUA",
            "DIAS_AGUA",
            "KEY_AGUA",
            8,
            0,
            "A cada 2h",
            "Beber Água 💧",
            "Mantenha-se hidratado!",
            prefs
        )

        configurarLembrete(
            binding.cardDormir,
            binding.switchDormir,
            binding.txtHoraDormir,
            ID_DORMIR,
            "HORA_DORMIR",
            "MIN_DORMIR",
            "DIAS_DORMIR",
            "KEY_DORMIR",
            22,
            30,
            "Todos os dias",
            "Hora de Dormir 😴",
            "Boa noite!",
            prefs
        )
    }


    private fun abrirDialogoAgua(isChecked: Boolean) {

        val prefs = requireContext().getSharedPreferences(
            "LembretesPrefs",
            Context.MODE_PRIVATE
        )

        val horaInicioSalva = prefs.getInt("HORA_AGUA_INICIO", 8)
        val minutoInicioSalvo = prefs.getInt("MIN_AGUA_INICIO", 0)

        val horaFimSalva = prefs.getInt("HORA_AGUA_FIM", 22)
        val minutoFimSalvo = prefs.getInt("MIN_AGUA_FIM", 0)

        val intervaloSalvo = prefs.getInt("INTERVALO_AGUA", 2)

        val diasSalvos = prefs.getString(
            "DIAS_AGUA",
            "Todos os dias"
        ) ?: "Todos os dias"

        TimePickerDialog(
            requireContext(),
            { _, horaInicio, minutoInicio ->

                TimePickerDialog(
                    requireContext(),
                    { _, horaFim, minutoFim ->

                        val inicio = horaInicio * 60 + minutoInicio
                        val fim = horaFim * 60 + minutoFim

                        if (fim <= inicio) {

                            Toast.makeText(
                                requireContext(),
                                "O horário final deve ser posterior ao inicial.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@TimePickerDialog
                        }

                        val intervalos = intArrayOf(1, 2, 3, 4)

                        val opcoesIntervalo = arrayOf(
                            "A cada 1 hora",
                            "A cada 2 horas",
                            "A cada 3 horas",
                            "A cada 4 horas"
                        )

                        var intervaloSelecionado =
                            intervalos.indexOf(intervaloSalvo)
                                .coerceAtLeast(0)

                        AlertDialog.Builder(requireContext())
                            .setTitle("Intervalo das notificações")
                            .setSingleChoiceItems(
                                opcoesIntervalo,
                                intervaloSelecionado
                            ) { _, which ->
                                intervaloSelecionado = which
                            }
                            .setPositiveButton("Continuar") { _, _ ->

                                val opcoesDias = arrayOf(
                                    "Todos os dias",
                                    "Seg a Sex",
                                    "Fins de semana"
                                )

                                var diaSelecionado =
                                    opcoesDias.indexOf(diasSalvos)
                                        .coerceAtLeast(0)

                                AlertDialog.Builder(requireContext())
                                    .setTitle("Repetir em quais dias?")
                                    .setSingleChoiceItems(
                                        opcoesDias,
                                        diaSelecionado
                                    ) { _, which ->
                                        diaSelecionado = which
                                    }
                                    .setPositiveButton("Salvar") { _, _ ->

                                        val dias = opcoesDias[diaSelecionado]

                                        prefs.edit()
                                            .putInt(
                                                "HORA_AGUA_INICIO",
                                                horaInicio
                                            )
                                            .putInt(
                                                "MIN_AGUA_INICIO",
                                                minutoInicio
                                            )
                                            .putInt(
                                                "HORA_AGUA_FIM",
                                                horaFim
                                            )
                                            .putInt(
                                                "MIN_AGUA_FIM",
                                                minutoFim
                                            )
                                            .putInt(
                                                "INTERVALO_AGUA",
                                                intervalos[intervaloSelecionado]
                                            )
                                            .putString("DIAS_AGUA", dias)
                                            .apply()

                                        atualizarTextoAgua(prefs)

                                        if (isChecked) {
                                            LembreteScheduler.agendarProximoLembreteAgua(
                                                requireContext()
                                            )
                                        }

                                        Toast.makeText(
                                            requireContext(),
                                            "Lembrete de água configurado!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .setNegativeButton("Cancelar", null)
                                    .show()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    },
                    horaFimSalva,
                    minutoFimSalvo,
                    true
                ).apply {
                    setTitle("Horário final")
                }.show()
            },
            horaInicioSalva,
            minutoInicioSalvo,
            true
        ).apply {
            setTitle("Horário inicial")
        }.show()
    }


    private fun configurarLembrete(
        card: View,
        switch: CompoundButton,
        textoHora: TextView,
        id: Int,
        keyHora: String,
        keyMin: String,
        keyDias: String,
        keySwitch: String,
        horaPadrao: Int,
        minutoPadrao: Int,
        diasPadrao: String,
        titulo: String,
        mensagem: String,
        prefs: android.content.SharedPreferences
    ) {

        card.setOnClickListener {

            if (id == ID_AGUA) {

                abrirDialogoAgua(switch.isChecked)

            } else {

                abrirDialogoEdicao(
                    id,
                    keyHora,
                    keyMin,
                    keyDias,
                    horaPadrao,
                    minutoPadrao,
                    diasPadrao,
                    titulo,
                    mensagem,
                    textoHora,
                    switch.isChecked
                )
            }
        }

        switch.setOnCheckedChangeListener { _, marcado ->

            salvarEstadoSwitch(keySwitch, marcado)

            if (marcado) {

                if (id == ID_AGUA) {

                    LembreteScheduler.agendarProximoLembreteAgua(
                        requireContext()
                    )

                } else {

                    agendarSeguro(
                        id,
                        prefs.getInt(keyHora, horaPadrao),
                        prefs.getInt(keyMin, minutoPadrao),
                        titulo,
                        mensagem
                    )
                }

            } else {

                cancelarSeguro(id)
            }
        }
    }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
