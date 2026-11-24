
package com.example.vita.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.vita.R

class LembreteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getIntExtra("EXTRA_ID", 0)

        val titulo = intent.getStringExtra("EXTRA_TITULO")
            ?: "Lembrete Vita"

        val mensagem = intent.getStringExtra("EXTRA_MENSAGEM")
            ?: "Hora do seu lembrete!"

        val hora = intent.getIntExtra("EXTRA_HORA", 8)
        val minuto = intent.getIntExtra("EXTRA_MINUTO", 0)

        val lembreteAgua = intent.getBooleanExtra(
            "EXTRA_AGUA",
            false
        )

        // Agenda a próxima ocorrência antes de verificar
        // a permissão de notificação.
        if (lembreteAgua) {

            LembreteScheduler.agendarProximoLembreteAgua(context)

        } else {

            // Mantém a repetição diária dos outros lembretes.
            LembreteScheduler.agendarLembrete(
                context,
                id,
                hora,
                minuto,
                titulo,
                mensagem
            )
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LembreteScheduler.criarCanalNotificacao(context)

        val notification = NotificationCompat.Builder(
            context,
            LembreteScheduler.CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            id,
            notification
        )
    }
}
