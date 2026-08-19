package com.lumi.fpsaddons

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

    /** Pointer speed: -7 (chậm nhất) đến 7 (nhanh nhất). Tăng nhẹ giúp thao tác mượt hơn. */
    fun setPointerSpeed(speed: Int = 5): String =
        ShizukuHelper.runCommand("settings put system pointer_speed $speed")

    fun resetPointerSpeed(): String =
        ShizukuHelper.runCommand("settings put system pointer_speed 0")

    /** "Tối ưu tần số quét" — gộp buff refresh rate + tăng pointer speed cho mượt tay. */
    fun optimizeScreen(rate: Float = 120f, pointerSpeed: Int = 5): String {
        val log = StringBuilder()
        log.appendLine("--- Tối ưu tần số quét ---")
        log.appendLine(boostRefreshRate(rate))
        log.appendLine("--- Tăng tốc độ con trỏ (point speed) ---")
        log.appendLine(setPointerSpeed(pointerSpeed))
        return log.toString()
    }

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
