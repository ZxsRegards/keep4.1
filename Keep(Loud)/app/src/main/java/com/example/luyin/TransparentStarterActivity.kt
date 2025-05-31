package com.example.luyin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TransparentStarterActivity : AppCompatActivity() {
    private val TAG = "TransparentStarter"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "透明Activity已创建，准备启动后台服务")
        // 启动音频录制服务
        val serviceIntent = Intent(this, AudioRecordService::class.java).apply {
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Android 8.0+ 系统，使用startForegroundService")
                startForegroundService(serviceIntent)
            } else {
                Log.d(TAG, "低版本系统，使用普通startService")
                startService(serviceIntent)
            }
        } catch (e: Exception) {
        }
        finish()
    }
}
