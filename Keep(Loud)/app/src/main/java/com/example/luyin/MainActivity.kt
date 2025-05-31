package com.example.luyin

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO = 1
    private val REQUEST_OVERLAY = 1003
    private lateinit var floatingBallManager: FloatingBallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingBallManager = FloatingBallManager(this)//悬浮窗
        setupCardClickListeners()
        checkAndRequestPermissions()
        checkAndHandleOverlayPermission()
    }




    private fun isAccessibilityEnabled(): Boolean {
        val service = ComponentName(this, AppWatcherService::class.java)
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabled?.contains(service.flattenToString()) ?: false
    }

    private fun showEnableDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("申请无障碍权限")
            .setMessage("无障碍权限只为了保活，如介意请勿开启无障碍权限。")
            .setPositiveButton("去开启") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("取消", null)
            .create()
        // 禁用返回键关闭
        dialog.setCancelable(false)
        // 禁用点击外部关闭
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }




    private fun checkAndHandleOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//首先检查Android版本是否>=6.0,首先检查Android版本是否>=6.0
            if (Settings.canDrawOverlays(this)) {
            //    floatingBallManager.start()  //已授权：调用createHiddenBall()创建悬浮球
            } else {
                checkOverlayPermission()////未授权：调用checkOverlayPermission()请求权限
            }
        } else {
            //floatingBallManager.start()
        }
    }
    private fun checkOverlayPermission() { //主动请求悬浮窗权限
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OVERLAY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                    //    floatingBallManager.start() //检查用户是否最终授予了权限,如果已授权：创建悬浮球
                    } else {
                        Toast.makeText(this, "悬浮球功能需要悬浮窗权限", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    override fun onDestroy() {

        super.onDestroy()
    }

    private fun setupCardClickListeners() {
        val card1 = findViewById<CardView>(R.id.zxs1)
        val card2 = findViewById<CardView>(R.id.zxs2)
        val card3 = findViewById<CardView>(R.id.zxs3)
        val card4 = findViewById<CardView>(R.id.zxs4)
        val card5 = findViewById<CardView>(R.id.zxs5)
        card1.setOnClickListener { openBrowser(this, "https://github.com/aimmarc/keep_4") }
        card2.setOnClickListener { openBrowser(this, "https://github.com/ZxsRegards/keep4.1") }
        card3.setOnClickListener { startActivity(Intent(this, WebViewActivity::class.java)) }
        card4.setOnClickListener { AutoStartUtils.openAutoStartSetting(this) }
        card5.setOnClickListener {
            if (!isAccessibilityEnabled())
            {
                showEnableDialog()
        } else {
            Toast.makeText(this, "无障碍已启用", Toast.LENGTH_SHORT).show()
        } }
    }

    private fun openBrowser(context: Context, url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>().apply {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.RECORD_AUDIO)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), REQUEST_RECORD_AUDIO)
        } else {
            startRecordingService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startRecordingService()
                } else {
                    Toast.makeText(this, "需要权限才能录音", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startRecordingService() {
        val serviceIntent = Intent(this, AudioRecordService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "录音服务已启动", Toast.LENGTH_SHORT).show()
    }
}
