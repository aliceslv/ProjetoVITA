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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun agendarLembrete(
        context: Context,
        id: Int,
        hora: Int,
        minuto: Int,
        titulo: String,
        mensagem: String
    ) {
        criarCanalNotificacao(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra("EXTRA_TITULO", titulo)
            putExtra("EXTRA_MENSAGEM", mensagem)
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_HORA", hora)
            putExtra("EXTRA_MINUTO", minuto)
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

    fun cancelarLembrete(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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