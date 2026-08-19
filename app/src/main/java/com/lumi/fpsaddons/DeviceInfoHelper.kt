package com.lumi.fpsaddons

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs

/** Thông tin máy thật — kiểu CPU-Z, tiếng Việt, đọc từ hệ thống Android chuẩn. */
object DeviceInfoHelper {

    fun getCpuInfo(): String {
        val raw = ShizukuHelper.runCommand("cat /proc/cpuinfo | grep -m1 -E 'Hardware|model name'")
        val cores = Runtime.getRuntime().availableProcessors()
        val chip = raw.substringAfter(":", raw).trim().ifBlank { "Không xác định" }
        return "Chip: $chip\nSố nhân CPU: $cores"
    }

    fun getTemperature(): String {
        val raw = ShizukuHelper.runCommand("cat /sys/class/thermal/thermal_zone0/temp").trim()
        val value = raw.toIntOrNull() ?: return "Không đọc được nhiệt độ"
        val celsius = if (value > 1000) value / 1000.0 else value.toDouble()
        return "%.1f°C".format(celsius)
    }

    fun getRamInfo(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalGB = mi.totalMem / (1024.0 * 1024 * 1024)
        val availGB = mi.availMem / (1024.0 * 1024 * 1024)
        return "Tổng RAM: %.1f GB\nCòn trống: %.1f GB".format(totalGB, availGB)
    }

    fun getStorageInfo(): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalGB = stat.totalBytes / (1024.0 * 1024 * 1024)
        val availGB = stat.availableBytes / (1024.0 * 1024 * 1024)
        return "Tổng bộ nhớ: %.1f GB\nCòn trống: %.1f GB".format(totalGB, availGB)
    }

    fun getBatteryInfo(context: Context): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val temp = try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val t = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (t >= 0) "%.1f°C".format(t / 10.0) else "?"
        } catch (e: Exception) { "?" }
        return "Pin: $level%\nNhiệt độ pin: $temp"
    }
}
