package com.lumi.fpsaddons

/**
 * LƯU Ý QUAN TRỌNG (đọc trước khi thêm lệnh mới):
 * - Chỉ động vào thư mục "cache", KHÔNG BAO GIỜ động vào "games/com.mojang"
 *   (đó là nơi lưu world của người chơi, xoá nhầm là mất map).
 * - Tắt animation hệ thống là tối ưu THẬT, có tác dụng rõ (giảm độ trễ UI).
 * - "Giải phóng RAM" bằng cách force-stop app khác chỉ có tác dụng NHẸ và
 *   TẠM THỜI — Android sẽ tự mở lại các app đó khi cần, đừng quảng cáo
 *   quá đà với người dùng là "tăng gấp đôi RAM" hay tương tự.
 */
object OptimizeTasks {

    /** Xoá cache của Minecraft (an toàn — không đụng world/save) */
    fun clearMinecraftCache(): String {
        val cacheDir = "/storage/emulated/0/Android/data/com.mojang.minecraftpe/cache"
        return ShizukuHelper.runCommand("rm -rf $cacheDir/* 2>/dev/null; echo Đã xoá cache")
    }

    /** Tắt animation hệ thống — giảm giật lag khi chuyển màn hình, mở app */
    fun disableSystemAnimations(): String {
        val cmds = listOf(
            "settings put global window_animation_scale 0",
            "settings put global transition_animation_scale 0",
            "settings put global animator_duration_scale 0"
        ).joinToString(" && ")
        return ShizukuHelper.runCommand(cmds)
    }

    /** Trả animation về mặc định nếu người dùng muốn hoàn tác */
    fun restoreSystemAnimations(): String {
        val cmds = listOf(
            "settings put global window_animation_scale 1",
            "settings put global transition_animation_scale 1",
            "settings put global animator_duration_scale 1"
        ).joinToString(" && ")
        return ShizukuHelper.runCommand(cmds)
    }

    /**
     * Force-stop một danh sách package cụ thể (KHÔNG quét toàn máy tự động —
     * để người dùng tự chọn app nào muốn tắt, tránh tắt nhầm app hệ thống
     * và làm treo máy).
     */
    fun forceStopApps(packageNames: List<String>): String {
        val cmds = packageNames.joinToString(" ; ") { "am force-stop $it" }
        return ShizukuHelper.runCommand(cmds)
    }

    /**
     * Xoá cache cho danh sách app do người dùng chọn (chỉ xoá thư mục cache
     * bên ngoài, KHÔNG dùng "pm clear" vì lệnh đó xoá sạch data app —
     * mất đăng nhập, mất tiến trình game... rất nguy hiểm).
     */
    fun clearCacheForApps(packageNames: List<String>): String {
        val log = StringBuilder()
        for (pkg in packageNames) {
            val cacheDir = "/storage/emulated/0/Android/data/$pkg/cache"
            val result = ShizukuHelper.runCommand("rm -rf $cacheDir/* 2>/dev/null; echo OK")
            log.appendLine("- $pkg: $result")
        }
        log.appendLine("=== Đã xoá cache xong ===")
        return log.toString()
    }
}
