
package com.example.luyin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

class AutoStartReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AutoStartReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "收到开机广播")
            handleBatteryOptimization(context)
            startRecordingService(context)
        }
    }

    private fun handleBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                try {
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(this)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "打开电池优化设置失败", e)
                }
            }
        }
    }

    private fun startRecordingService(context: Context) {
        try {
            Intent(context, AudioRecordService::class.java).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(this)
                } else {
                    context.startService(this)
                }
            }
            Log.d(TAG, "成功启动录音服务")
        } catch (e: Exception) {
            Log.e(TAG, "启动服务失败", e)
        }
    }
}
