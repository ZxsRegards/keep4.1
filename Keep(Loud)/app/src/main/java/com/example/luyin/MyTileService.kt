
package com.example.luyin

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

class MyTileService : TileService() {

    companion object {
        private const val TAG = "MyTileService"
        private const val SERVICE_CLASS = "com.example.luyin.AudioRecordService"
        private const val NOTIFICATION_CHANNEL_ID = "tile_service_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onClick() {
        super.onClick()
        when {
            isAppKilled() -> {
                showNotification("需要用户操作", "请前往应用界面启动录音服务")
                startPermissionGuideActivity()
            }
            isServiceRunning() -> {
                showNotification("录音服务已在运行", "开启四扬功能已开启,无需二次启动")
                updateTileState(Tile.STATE_ACTIVE)
            }
            else -> {
                showNotification("启动录音服务", "正在启动录音服务...")
                startServiceSafely()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "磁贴服务通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示磁贴操作状态"
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }


    private fun showNotification(
        title: String,
        message: String,
        @DrawableRes icon: Int = android.R.drawable.ic_dialog_info,
        isForeground: Boolean = false
    ) {
        // 创建跳转Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知渠道（兼容Android O+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "enhanced_channel",
                "服务状态通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "重要服务状态变更通知"
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = Color.RED
            }.also { channel ->
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
            }
        }

        // 构建通知
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "enhanced_channel")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.apply {
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(icon)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            setVibrate(longArrayOf(0, 500, 200, 500))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setCategory(Notification.CATEGORY_ALARM)
                setVisibility(Notification.VISIBILITY_PUBLIC)
                setLights(Color.RED, 1000, 1000)
            }
        }.build()

        // 显示通知
        if (isForeground) {
            startForeground(999, notification)
        } else {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, notification)
        }
    }



    private fun isAppKilled(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.appTasks.isEmpty() && !isServiceRunning()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == SERVICE_CLASS }
    }

    private fun startPermissionGuideActivity() {
        val intent = Intent(this, PermissionGuideActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("REQUEST_TYPE", "FOREGROUND_SERVICE")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        startActivityAndCollapse(pendingIntent)
    }

    private fun startServiceSafely() {
        try {
            val intent = Intent(this, AudioRecordService::class.java)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    startForegroundService(intent)
                }
                else -> {
                    startService(intent)
                }
            }
            updateTileState(Tile.STATE_ACTIVE)
            showNotification("服务已启动", "录音服务正在运行")
        } catch (e: Exception) {
            handleStartError(e)
            updateTileState(Tile.STATE_INACTIVE)
            showNotification("启动失败", "无法启动录音服务: ${e.localizedMessage}")
        }
    }

    private fun updateTileState(state: Int) {
        qsTile?.let { tile ->
            tile.state = state
            tile.updateTile()
        }
    }

    private fun handleStartError(e: Exception) {
        Log.e(TAG, "Service start failed", e)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                "服务启动失败: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
