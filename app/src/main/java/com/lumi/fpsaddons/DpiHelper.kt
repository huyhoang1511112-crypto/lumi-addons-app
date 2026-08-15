package com.lumi.fpsaddons

/**
 * Đổi DPI (mật độ điểm ảnh) hệ thống — lệnh "wm density".
 * Đây là lệnh chuẩn của Android, không phải hack gì — chính là lệnh
 * nằm trong "adb shell wm density 400" mà nhiều người vẫn dùng qua máy tính.
 * BẮT BUỘC cần quyền Shizuku vì cần WRITE_SECURE_SETTINGS, app thường không có.
 */
object DpiHelper {

    fun setDensity(dpi: Int): String = ShizukuHelper.runCommand("wm density $dpi")

    fun resetDensity(): String = ShizukuHelper.runCommand("wm density reset")

    fun getCurrentDensity(): String = ShizukuHelper.runCommand("wm density")
}
