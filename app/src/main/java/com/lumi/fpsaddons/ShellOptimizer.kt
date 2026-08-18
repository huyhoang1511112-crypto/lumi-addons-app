package com.lumi.fpsaddons

/**
 * Tổng hợp lệnh shell tối ưu hiệu năng — TẤT CẢ đều là lệnh chuẩn của Android
 * (giống hệt lệnh chạy qua "adb shell" từ máy tính), không phải hack/exploit.
 * Toàn bộ đều an toàn, có thể hoàn tác, không đụng dữ liệu người dùng.
 */
object ShellOptimizer {

    private const val FREE_FIRE_PKG = "com.dts.freefireth"

    /** Android 12+: bật "Game Mode - Hiệu năng" cho 1 app cụ thể (ưu tiên CPU/GPU) */
    fun enableGamePerformanceMode(pkg: String = FREE_FIRE_PKG): String =
        ShizukuHelper.runCommand("cmd game mode set --mode 2 $pkg")

    fun disableGameMode(pkg: String = FREE_FIRE_PKG): String =
        ShizukuHelper.runCommand("cmd game mode set --mode 0 $pkg")

    /** Android 10+: giữ hiệu năng ổn định, tránh CPU/GPU tự hạ xung liên tục gây giật cục */
    fun enableSustainedPerformance(): String =
        ShizukuHelper.runCommand("cmd power set-fixed-performance-mode-enabled true")

    fun disableSustainedPerformance(): String =
        ShizukuHelper.runCommand("cmd power set-fixed-performance-mode-enabled false")

    /** Tắt tạm chế độ tiết kiệm pin trong lúc chơi (không tắt vĩnh viễn, chỉ tắt cờ hiện tại) */
    fun disableBatterySaver(): String =
        ShizukuHelper.runCommand("settings put global low_power 0")

    /** Cờ Developer Options cũ "Force GPU rendering" — ép vẽ UI bằng GPU thay vì CPU */
    fun forceGpuRendering(): String =
        ShizukuHelper.runCommand("settings put global force_gpu_rendering 1")

    fun resetGpuRendering(): String =
        ShizukuHelper.runCommand("settings put global force_gpu_rendering 0")

    /**
     * "Buff Màn" — ép tần số quét màn hình lên mức cao nhất máy hỗ trợ.
     * Lệnh chuẩn Android 11+ (peak_refresh_rate / min_refresh_rate).
     * Máy không hỗ trợ tần số cao thì lệnh chạy nhưng không có tác dụng thấy rõ.
     */
    fun boostRefreshRate(rate: Float = 120f): String =
        ShizukuHelper.runCommand(
            "settings put system peak_refresh_rate $rate && settings put system min_refresh_rate $rate"
        )

    fun resetRefreshRate(): String =
        ShizukuHelper.runCommand(
            "settings delete system peak_refresh_rate && settings delete system min_refresh_rate"
        )

    /**
     * "Fix Lag FPS" — chạy 1 lượt tổ hợp các lệnh tối ưu chính, log từng bước rõ ràng.
     */
    fun quickFixLag(): String {
        val log = StringBuilder()
        log.appendLine("--- Tắt animation hệ thống ---")
        log.appendLine(OptimizeTasks.disableSystemAnimations())
        log.appendLine("--- Bật Game Mode hiệu năng (Free Fire) ---")
        log.appendLine(enableGamePerformanceMode())
        log.appendLine("--- Bật hiệu năng ổn định ---")
        log.appendLine(enableSustainedPerformance())
        log.appendLine("--- Tắt tiết kiệm pin tạm thời ---")
        log.appendLine(disableBatterySaver())
        log.appendLine("--- Ép vẽ bằng GPU ---")
        log.appendLine(forceGpuRendering())
        log.appendLine("=== HOÀN TẤT FIX LAG FPS ===")
        return log.toString()
    }
}

