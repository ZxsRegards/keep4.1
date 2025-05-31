package com.example.luyin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class AudioRecordService : Service() {
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var scheduler: ScheduledExecutorService
    private lateinit var task: Runnable
    private var notificationManager: NotificationManager? = null
    private val CHANNEL_ID = "RecordingChannel"
    private lateinit var floatingBallManager: FloatingBallManager

    override fun onCreate() {
        getSharedPreferences("tile_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("service_running", false)
            .apply()
        super.onCreate()
        floatingBallManager = FloatingBallManager(this)
        createNotificationChannel()
        initRecorder()//录音初始化
        startMonitoring()//检测音频状态
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification("录音服务运行中，悬浮窗已启动")
        floatingBallManager.start()
        return START_STICKY
    }

    private fun showNotification(message: String, @DrawableRes customIcon: Int = android.R.drawable.ic_dialog_info) {
        // 创建跳转到主Activity的PendingIntent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 创建通知渠道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "urgent_channel",
                "录音状态通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "高优先级系统通知"
                setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        // 构建通知（添加点击事件）
        val notification = NotificationCompat.Builder(this, "urgent_channel")
            .setContentTitle("录音状态通知")
            .setContentText(message)
            .setSmallIcon(customIcon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // 添加点击事件
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setLights(Color.RED, 1000, 1000)
            .build()
        startForeground(999, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recording Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows recording status"
            }
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun initRecorder() {
        try {
            val outputDir = getExternalFilesDir(null) ?: run {
                return
            }
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("${outputDir.absolutePath}/recording_${System.currentTimeMillis()}.3gp")
                prepare()
            }
            Log.d("AudioService", "录音器初始化成功")
        } catch (e: Exception) {
            Log.e("AudioService", "录音初始化失败: ${e.message}")
            // 重要：初始化失败时重置状态
            isRecording = false
            showToast("录音初始化失败,请授权录音通知权限${e.message}")
        }
    }
    private fun startMonitoring() {
        scheduler = Executors.newScheduledThreadPool(1)
        task = Runnable {
            try {
                val isPlaying = isAudioPlaying()
                val volumeLevel = getCurrentVolumeLevel()

                if (!isRecording && isPlaying && volumeLevel > 0) {
                    Log.d("AudioService", "检测到音频播放且音量大于0，开始录音")
                    startRecording()
                }
                if (isRecording && (!isPlaying || volumeLevel == 0)) {
                    Log.d("AudioService", "音频停止或音量为0，停止录音")
                    stopRecording()
                    //initRecorder()
                }
            } catch (e: Exception) {
                Log.e("AudioService", "监控任务异常: ${e.message}")
            }
        }
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS)
    }

    //添加了getCurrentVolumeLevel()方法检测当前媒体音量
    private fun getCurrentVolumeLevel(): Int {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }


    private fun isAudioPlaying(): Boolean {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return audioManager.isMusicActive
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(getExternalFilesDir(null)?.absolutePath + "/recording.3gp")
            try {
                prepare()
                start()
                isRecording = true
                Log.d("AudioService", "开始录音")
                showNotification("录音开始")
            } catch (e: Exception) {
                Log.e("AudioService", "录音启动失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        try {
            if (::mediaRecorder.isInitialized) {
                mediaRecorder.apply {
                    stop()
                    release()
                }
                Log.d("AudioService", "录音已停止并释放资源")
            }
        } catch (e: IllegalStateException) {
            Log.e("AudioService", "停止录音时状态异常: ${e.message}")
        } finally {
            val volumeLevel = getCurrentVolumeLevel()
            isRecording = false
            if ( volumeLevel == 0 ) {
                showNotification("媒体已静音，录音停止")
            }
            else {
                showNotification("录音停止")
            }
            // 确保重新初始化
            initRecorder()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    //    Log.d("录音服务", "服务销毁处理中...")
        if (isRecording) {
    //        Log.d("录音服务", "检测到正在录音，正在停止...")
            stopRecording()
        }
        scheduler.shutdown()
    //    Log.d("录音服务", "任务调度器已关闭")
        mediaRecorder.release()
    //    Log.d("录音服务", "音频录制器资源已释放")
        stopForeground(true)
    //    Log.d("录音服务", "前台服务通知已移除")
    //    Log.d("录音服务", "服务销毁完成")
    }
}