package com.lumi.fpsaddons

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Từ Android 11+, app thường không được ghi trực tiếp vào thư mục
 * dữ liệu riêng của Minecraft (scoped storage chặn). Nhưng lệnh shell
 * chạy qua Shizuku (uid=shell) thì KHÔNG bị chặn — đây chính là lý do
 * cần Shizuku cho tính năng này, không phải để "hack" gì cả.
 */
object AddonInstaller {

    // Đường dẫn chứa file game của Minecraft Bedrock trên hầu hết máy Android hiện nay
    private const val MC_GAMES_DIR =
        "/storage/emulated/0/games/com.mojang"

    /**
     * B1: Copy file addon người dùng chọn (content:// Uri) vào cache riêng
     * của app (không cần quyền đặc biệt, ContentResolver làm được).
     * B2: Dùng Shizuku shell "cp" để chuyển từ cache app sang thư mục Minecraft.
     */
    fun installAddon(context: Context, addonUri: Uri, fileName: String): String {
        // B1 — copy vào cache app
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(addonUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return "Không đọc được file đã chọn"

        // B2 — dùng Shizuku để copy sang thư mục Minecraft
        val targetDir = if (fileName.endsWith(".mcaddon")) {
            MC_GAMES_DIR
        } else {
            "$MC_GAMES_DIR/behavior_packs"
        }

        val result = ShizukuHelper.runCommand(
            "mkdir -p $targetDir && cp '${tempFile.absolutePath}' '$targetDir/$fileName'"
        )

        tempFile.delete()
        return result
    }
}
