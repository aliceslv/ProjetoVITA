
package com.example.vita.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object LembreteScheduler {

    const val CHANNEL_ID = "lembretes_vita_channel"
    const val ID_AGUA = 105

    fun criarCanalNotificacao(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lembretes do Vita",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de lembretes do aplicativo"
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    // Continua sendo usado pelos lembretes de refeições e sono.
    fun agendarLembrete(
        context: Context,
        id: Int,
        hora: Int,
        minuto: Int,
        titulo: String,
        mensagem: String
    ) {
        criarCanalNotificacao(context)

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra("EXTRA_TITULO", titulo)
            putExtra("EXTRA_MENSAGEM", mensagem)
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_HORA", hora)
            putExtra("EXTRA_MINUTO", minuto)
            putExtra("EXTRA_AGUA", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Agenda somente a próxima notificação de água.
    fun agendarProximoLembreteAgua(context: Context) {

        val prefs = context.getSharedPreferences(
            "LembretesPrefs",
            Context.MODE_PRIVATE
        )

        if (!prefs.getBoolean("KEY_AGUA", true)) {
            return
        }

        val horaInicio = prefs.getInt("HORA_AGUA_INICIO", 8)
        val minutoInicio = prefs.getInt("MIN_AGUA_INICIO", 0)

        val horaFim = prefs.getInt("HORA_AGUA_FIM", 22)
        val minutoFim = prefs.getInt("MIN_AGUA_FIM", 0)

        val intervalo = prefs.getInt("INTERVALO_AGUA", 2)

        val dias = prefs.getString(
            "DIAS_AGUA",
            "Todos os dias"
        ) ?: "Todos os dias"

        if (intervalo <= 0) return

        val inicioEmMinutos = horaInicio * 60 + minutoInicio
        val fimEmMinutos = horaFim * 60 + minutoFim

        // O período deve começar e terminar no mesmo dia.
        if (fimEmMinutos <= inicioEmMinutos) return

        val proximoHorario = encontrarProximoHorario(
            inicioEmMinutos,
            fimEmMinutos,
            intervalo,
            dias
        ) ?: return

        criarCanalNotificacao(context)

        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra("EXTRA_ID", ID_AGUA)
            putExtra("EXTRA_TITULO", "Beber Água 💧")
            putExtra("EXTRA_MENSAGEM", "Mantenha-se hidratado!")
            putExtra("EXTRA_AGUA", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_AGUA,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    proximoHorario.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    proximoHorario.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun encontrarProximoHorario(
        inicio: Int,
        fim: Int,
        intervalo: Int,
        dias: String
    ): Calendar? {

        val agora = Calendar.getInstance()

        // Procura o próximo horário nos próximos oito dias.
        for (dia in 0..7) {

            val data = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dia)
            }

            val diaDaSemana = data.get(Calendar.DAY_OF_WEEK)

            val diaPermitido = when (dias) {
                "Seg a Sex" ->
                    diaDaSemana in Calendar.MONDAY..Calendar.FRIDAY

                "Fins de semana" ->
                    diaDaSemana == Calendar.SATURDAY ||
                            diaDaSemana == Calendar.SUNDAY

                else -> true
            }

            if (!diaPermitido) continue

            var horario = inicio

            while (horario <= fim) {

                val candidato = Calendar.getInstance().apply {
                    set(Calendar.YEAR, data.get(Calendar.YEAR))
                    set(Calendar.DAY_OF_YEAR, data.get(Calendar.DAY_OF_YEAR))
                    set(Calendar.HOUR_OF_DAY, horario / 60)
                    set(Calendar.MINUTE, horario % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (candidato.after(agora)) {
                    return candidato
                }

                horario += intervalo * 60
            }
        }

        return null
    }

    fun cancelarLembrete(context: Context, id: Int) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, LembreteReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
