package com.example.luyin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppWatcherService : AccessibilityService() {
    // 记录是否处于锁屏状态
    private var isLockScreenActive = false

    override fun onServiceConnected() {
        configureServiceSettings()
    }

    private fun configureServiceSettings() {
        val config = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = config
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.run {
            when (eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleWindowChange(event)
            }
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        when (event.className) {
            // 检测到锁屏界面
            "com.android.systemui.keyguard.KeyguardView" -> {
                isLockScreenActive = true
            }
            // 检测到桌面界面（解锁后）
            "com.android.launcher3.Launcher" -> {
                if (isLockScreenActive) {
                    executeUnlockAction()
                    isLockScreenActive = false
                }
            }
            // 兼容其他设备型号
            else -> {
                if (event.className?.contains("keyguard", ignoreCase = true) == true) {
                    isLockScreenActive = true
                } else if (isLockScreenActive && isHomeScreen(event)) {
                    executeUnlockAction()
                    isLockScreenActive = false
                }
            }
        }
    }

    private fun isHomeScreen(event: AccessibilityEvent): Boolean {
        return event.packageName?.equals("com.android.launcher3") == true ||
                event.packageName?.equals("com.google.android.apps.nexuslauncher") == true
    }

    private fun executeUnlockAction() {
        // 执行解锁后的操作
        startService(Intent(this, AudioRecordService::class.java))
    }

    override fun onInterrupt() {}
}
