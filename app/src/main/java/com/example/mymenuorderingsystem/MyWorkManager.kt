package com.example.mymenuorderingsystem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import timber.log.Timber

class OrderUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: MenuRepository
) : CoroutineWorker(appContext, workerParams) {
    private val CHANNEL_ID = "order_status_channel"

    override suspend fun doWork(): Result {
        val orderId = inputData.getString("ORDER_ID") ?: "未知"
        val note = inputData.getString("ORDER_NOTE") ?: "無備註"
        var items = repository.getMenuItems()

        return try {
            Timber.d("WorkManager: 正在上傳訂單 [$orderId], 備註為 [$note]")
            showNotification("訂單-[$orderId] 上傳成功", "備註: $note")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "order_status_channel"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "訂單狀態",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "通佑訂單處理進度"
            }
            Timber.d("workManager: 準備呼叫 notify() 發送通知")
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}