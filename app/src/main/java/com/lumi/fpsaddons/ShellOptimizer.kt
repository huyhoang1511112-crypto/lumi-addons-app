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

    fun setPointerSpeed(speed: Int = 5): String =
        ShizukuHelper.runCommand("settings put system pointer_speed $speed")

    fun resetPointerSpeed(): String =
        ShizukuHelper.runCommand("settings put system pointer_speed 0")

    fun optimizeScreen(rate: Float = 120f, pointerSpeed: Int = 5): String {
        val log = StringBuilder()
        log.appendLine("--- Tối ưu tần số quét ---")
        log.appendLine(boostRefreshRate(rate))
        log.appendLine("--- Tăng tốc độ con trỏ (point speed) ---")
        log.appendLine(setPointerSpeed(pointerSpeed))
        return log.toString()
    }

    fun setPrivateDns(host: String): String =
        ShizukuHelper.runCommand(
            "settings put global private_dns_mode hostname && settings put global private_dns_specifier $host"
        )

    fun resetPrivateDns(): String =
        ShizukuHelper.runCommand("settings put global private_dns_mode off")

    /** Đổi độ phân giải màn hình — lệnh "wm size", giảm tải GPU khi chơi game nặng */
    fun setResolution(width: Int, height: Int): String =
        ShizukuHelper.runCommand("wm size ${width}x${height}")

    fun resetResolution(): String =
        ShizukuHelper.runCommand("wm size reset")

    fun getCurrentResolution(): String =
        ShizukuHelper.runCommand("wm size")

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
