package com.example.luyin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PermissionGuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.getStringExtra("REQUEST_TYPE")) {
            "FOREGROUND_SERVICE" -> showServiceDialog()
        }
    }


    private fun showServiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Loud服务无法启动")
            .setMessage("请点击确认以启动录音服务")
            .setPositiveButton("启动") { _, _ ->
                startService(Intent(this, AudioRecordService::class.java))
                finishWithAnimation()
            }
            .setNegativeButton("") { _, _ ->
                finishWithAnimation()
            }
            .setCancelable(false)  // 关键设置：禁止外部取消
            .create()
            .apply {
                setCanceledOnTouchOutside(false)  // 禁止触摸外部取消
                show()
            }
    }


    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
