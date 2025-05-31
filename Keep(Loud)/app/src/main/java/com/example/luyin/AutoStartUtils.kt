package com.example.luyin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast

object AutoStartUtils {
    private const val TAG = "AutoStartUtils"
    private fun isXiaomi() = Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
            Build.MANUFACTURER.equals("redmi", ignoreCase = true)
    private fun isHuawei() = Build.MANUFACTURER.equals("huawei", ignoreCase = true)
    private fun isOppo() = Build.MANUFACTURER.equals("oppo", ignoreCase = true) ||
            Build.MANUFACTURER.equals("realme", ignoreCase = true)
    private fun isVivo() = Build.MANUFACTURER.equals("vivo", ignoreCase = true)
    private fun isHonor() = Build.MANUFACTURER.equals("honor", ignoreCase = true)

    fun openAutoStartSetting(context: Context, showToast: Boolean = true) {
        when {
            isXiaomi() -> try {
                Intent().apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                    context.startActivity(this)
                }
                return
            } catch (e: Exception) {
                tryXiaomiAlternative(context)
            }

            isHuawei() -> try {
                Intent().apply {
                    setClassName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                    context.startActivity(this)
                }
                return
            } catch (e: Exception) {
                tryHuaweiAlternative(context)
            }

            isOppo() -> try {
                Intent().apply {
                    setClassName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                    context.startActivity(this)
                }
                return
            } catch (e: Exception) {
                tryOppoAlternative(context)
            }

            isVivo() -> try {
                Intent().apply {
                    setClassName(
                        "com.vivo.permcenter",
                        "com.vivo.permcenter.autostart.AutoStartManagementActivity"
                    )
                    context.startActivity(this)
                }
                return
            } catch (e: Exception) {
                openAppDetails(context)
            }

            isHonor() -> try {
                Intent().apply {
                    setClassName(
                        "com.hihonor.systemmanager",
                        "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                    context.startActivity(this)
                }
                return
            } catch (e: Exception) {
                openAppDetails(context)
            }

            else -> openAppDetails(context)
        }

        if (showToast) {
            Toast.makeText(context, "请手动在系统设置中允许自启动", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun tryXiaomiAlternative(context: Context) {
        try {
            Intent().apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.appmanager.ApplicationsDetailsActivity"
                )
                putExtra("packageName", context.packageName)
                context.startActivity(this)
            }
        } catch (e: Exception) {
            openAppDetails(context)
        }
    }
    private fun tryHuaweiAlternative(context: Context) {
        try {
            Intent().apply {
                setClassName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
                context.startActivity(this)
            }
        } catch (e: Exception) {
            openAppDetails(context)
        }
    }
    private fun tryOppoAlternative(context: Context) {
        val intent = Intent().apply {
            // 新版本路径（ColorOS 12/13）
            setClassName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )

            // 添加 FLAG 避免跳转失败
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 备用路径（旧版本）
        val backupIntent = Intent().apply {
            setClassName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                context.startActivity(backupIntent)
            } catch (e: Exception) {
                // 终极回退方案
                openAppDetails(context)
            }
        }
    }



    // 打开应用详情页
    private fun openAppDetails(context: Context) {
        try {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                context.startActivity(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开应用详情页失败", e)
        }
    }

}
