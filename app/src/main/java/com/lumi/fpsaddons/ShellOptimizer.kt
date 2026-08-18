package com.lumi.fpsaddons

/**
 * Tổng hợp lệnh shell tối ưu hiệu năng — TẤT CẢ đều là lệnh chuẩn của Android
 * (giống hệt lệnh chạy qua "adb shell" từ máy tính), không phải hack/exploit.
 * Toàn bộ đều an toàn, có thể hoàn tác, không đụng dữ liệu người dùng.
 */
object ShellOptimizer {

    private const val FREE_FIRE_PKG = "com.dts.freefireth"

    fun enableGamePerformanceMode(pkg: String = FREE_FIRE_PKG): String =
        ShizukuHelper.runCommand("cmd game mode set --mode 2 $pkg")

    fun disableGameMode(pkg: String = FREE_FIRE_PKG): String =
        ShizukuHelper.runCommand("cmd game mode set --mode 0 $pkg")

    fun enableSustainedPerformance(): String =
        ShizukuHelper.runCommand("cmd power set-fixed-performance-mode-enabled true")

    fun disableSustainedPerformance(): String =
        ShizukuHelper.runCommand("cmd power set-fixed-performance-mode-enabled false")

    fun disableBatterySaver(): String =
        ShizukuHelper.runCommand("settings put global low_power 0")

    fun forceGpuRendering(): String =
        ShizukuHelper.runCommand("settings put global force_gpu_rendering 1")

    fun resetGpuRendering(): String =
        ShizukuHelper.runCommand("settings put global force_gpu_rendering 0")

    fun boostRefreshRate(rate: Float = 120f): String =
        ShizukuHelper.runCommand(
            "settings put system peak_refresh_rate $rate && settings put system min_refresh_rate $rate"
        )

    fun resetRefreshRate(): String =
        ShizukuHelper.runCommand(
            "settings delete system peak_refresh_rate && settings delete system min_refresh_rate"
        )

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

    fun startComboBoost(
        refreshRate: Float = 120f,
        touchPoints: List<AntiGhostTouch.TapPoint>,
        touchIntervalMs: Long = 3000,
        scope: kotlinx.coroutines.CoroutineScope
    ): String {
        val log = StringBuilder()
        log.appendLine("--- Buff tần số quét màn hình ---")
        log.appendLine(boostRefreshRate(refreshRate))
        log.appendLine("--- Bật chống liệt cảm ứng ---")
        AntiGhostTouch.start(touchPoints, touchIntervalMs, scope)
        log.appendLine("Đã bật.")
        return log.toString()
    }

    fun stopComboBoost(): String {
        val log = StringBuilder()
        log.appendLine("--- Tắt chống liệt cảm ứng ---")
        AntiGhostTouch.stop()
        log.appendLine("--- Trả tần số quét về mặc định ---")
        log.appendLine(resetRefreshRate())
        return log.toString()
    }
}
