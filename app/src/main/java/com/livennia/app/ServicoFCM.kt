package com.livennia.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ServicoFCM : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val titulo = message.notification?.title ?: "LivennIA"
        val corpo = message.notification?.body ?: message.data["mensagem"] ?: ""
        mostrarNotificacao(titulo, corpo)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Enviar o token para o backend (implementar quando houver endpoint)
    }

    private fun mostrarNotificacao(titulo: String, corpo: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val canalId = "livennia_canal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "LivennIA", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(canal)
        }
        val notificacao = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notificacao)
    }
}