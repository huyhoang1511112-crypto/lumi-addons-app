package com.lumi.fpsaddons

import android.content.Context
import android.net.Uri
import java.io.File

object AddonInstaller {

    private const val MC_GAMES_DIR = "/storage/emulated/0/games/com.mojang"

    /** Cách 1 — CÓ Shizuku: copy qua lệnh shell, bypass được scoped storage, chạy trên mọi bản Android */
    fun installViaShizuku(context: Context, addonUri: Uri, fileName: String): String {
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(addonUri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return "Không đọc được file đã chọn"

        val targetDir = if (fileName.endsWith(".mcaddon")) MC_GAMES_DIR else "$MC_GAMES_DIR/behavior_packs"

        val result = ShizukuHelper.runCommand(
            "mkdir -p $targetDir && cp '${tempFile.absolutePath}' '$targetDir/$fileName'"
        )
        tempFile.delete()
        return result
    }

    /**
     * Cách 2 — KHÔNG Shizuku: copy file trực tiếp bằng Java File I/O.
     * CẦN quyền "Cho phép quản lý toàn bộ file" (MANAGE_EXTERNAL_STORAGE) —
     * người dùng bật thủ công 1 lần trong Settings, sau đó app ghi file bình
     * thường như 1 file manager, không cần Shizuku/ADB/root.
     */
    fun installViaFileAccess(addonUri: Uri, context: Context, fileName: String): String {
        if (!android.os.Environment.isExternalStorageManager()) {
            return "Chưa được cấp quyền 'Quản lý toàn bộ file' — bấm nút cấp quyền trước."
        }
        return try {
            val targetDir = if (fileName.endsWith(".mcaddon")) {
                File(MC_GAMES_DIR)
            } else {
                File("$MC_GAMES_DIR/behavior_packs")
            }
            targetDir.mkdirs()
            val targetFile = File(targetDir, fileName)
            context.contentResolver.openInputStream(addonUri)?.use { input ->
                targetFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return "Không đọc được file đã chọn"
            "Đã cài vào: ${targetFile.absolutePath}"
        } catch (e: Exception) {
            "Lỗi khi copy file: ${e.message}"
        }
    }
}
