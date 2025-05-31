
package com.example.luyin

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FloatingBallManager(private val context: Context) {
    private val windowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private var floatingView: FrameLayout? = null
    private val logFile by lazy { File(context.filesDir, "floating_ball.log") }

    // 创建并启动隐藏悬浮球
    fun start() {
        if (floatingView != null) {
            return
        }

        try {
            floatingView = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(1, 1)
            }

            windowManager.addView(floatingView, createLayoutParams())
            Toast.makeText(context, "悬浮球启动成功", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            logStatus("悬浮球启动失败: ${e.message}", false)
            Toast.makeText(context, "悬浮球启动失败,可能没给权限${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            width = 1
            height = 1
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSPARENT
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }
    }

    // 增强版日志记录
    private fun logStatus(message: String, success: Boolean? = null) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        val status = when(success) {
            true -> "[成功]"
            false -> "[失败]"
            else -> "[信息]"
        }
        val logMsg = "$time $status $message\n"

        try {
            logFile.appendText(logMsg)
            Log.d("FloatingBall", logMsg.trim())
        } catch (e: Exception) {
            Log.e("FloatingBall", "日志记录失败: ${e.message}")
        }
    }

    fun stop() {
        try {
            floatingView?.let { windowManager.removeView(it) }
            logStatus("悬浮球已停止")
        } catch (e: Exception) {
            logStatus("悬浮球停止失败: ${e.message}", false)
        }
        floatingView = null
    }

    // 检查悬浮球状态
    fun isRunning(): Boolean {
        val running = floatingView != null
        logStatus("悬浮球状态检查: ${if(running) "运行中" else "未运行"}")
        return running
    }
}
