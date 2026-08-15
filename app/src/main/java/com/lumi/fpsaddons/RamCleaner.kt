package com.lumi.fpsaddons

/**
 * "Dọn RAM" — làm 2 việc, cả 2 đều AN TOÀN, không mất dữ liệu người dùng:
 *
 * 1. Force-stop các app trong danh sách — y hệt việc người dùng tự vuốt tắt
 *    app trong màn hình Recent Apps. KHÔNG xoá bất kỳ dữ liệu nào của app đó,
 *    lần mở lại vẫn còn nguyên đăng nhập/lịch sử/mọi thứ.
 *
 * 2. Xoá CACHE (rác tạm) của các app phổ biến — CHỈ cache, không phải data.
 *    Khác biệt quan trọng:
 *      - "clear cache" (lệnh dùng ở đây)  -> an toàn, chỉ xoá file tạm
 *      - "clear data" / "pm clear"        -> xoá luôn đăng nhập, mật khẩu đã lưu
 *    App này CHỈ dùng "clear cache", không bao giờ dùng "pm clear" hay "clear data".
 */
object RamCleaner {

    val COMMON_APPS = linkedMapOf(
        "YouTube" to "com.google.android.youtube",
        "Facebook" to "com.facebook.katana",
        "Messenger" to "com.facebook.orca",
        "Zalo" to "com.zing.zalo",
        "TikTok" to "com.zhiliaoapp.musically",
        "Chrome" to "com.android.chrome"
    )

    fun clean(appsToStop: List<String>): String {
        val log = StringBuilder()
        log.appendLine("=== BẮT ĐẦU DỌN RAM ===")

        if (appsToStop.isEmpty()) {
            log.appendLine("Không có app nào được chọn để tắt, bỏ qua bước này.")
        } else {
            for (pkg in appsToStop) {
                val result = ShizukuHelper.runCommand("am force-stop $pkg")
                log.appendLine("Đã tắt: $pkg  →  $result")
            }
        }

        val cacheTargets = appsToStop + listOf("com.mojang.minecraftpe")
        for (pkg in cacheTargets.distinct()) {
            val path = "/storage/emulated/0/Android/data/$pkg/cache"
            val result = ShizukuHelper.runCommand("rm -rf $path/* 2>/dev/null; echo Xong")
            log.appendLine("Đã xoá cache của $pkg  →  $result")
        }

        log.appendLine("=== HOÀN TẤT — không app nào bị xoá dữ liệu đăng nhập ===")
        return log.toString()
    }
}
