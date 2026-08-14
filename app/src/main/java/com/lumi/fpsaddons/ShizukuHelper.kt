package com.lumi.fpsaddons

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Gói toàn bộ logic liên quan tới Shizuku vào 1 chỗ:
 * - Kiểm tra Shizuku app đã chạy chưa
 * - Xin quyền (giống ZArchiver: Shizuku sẽ hiện popup Cho phép / Từ chối)
 * - Chạy lệnh shell qua quyền ADB mà Shizuku cấp
 */
object ShizukuHelper {

    const val REQUEST_CODE = 1001

    /** Shizuku (app) có đang chạy và sẵn sàng nhận yêu cầu không */
    fun isShizukuAvailable(): Boolean = Shizuku.pingBinder()

    /** App này đã được cấp quyền trong Shizuku chưa */
    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    /** Gọi hàm này khi bấm nút "Xin quyền Shizuku" — sẽ bật popup cấp quyền */
    fun requestPermission() {
        if (isShizukuAvailable() && !hasPermission()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    /**
     * Chạy 1 lệnh shell với quyền Shizuku cấp (uid=shell, KHÔNG phải root,
     * nhưng cao hơn app thường — đủ để copy file vào thư mục app khác,
     * force-stop app, đổi settings hệ thống).
     * Trả về output (stdout + stderr) dạng String để hiện log cho người dùng xem.
     */
    fun runCommand(command: String): String {
        if (!hasPermission()) return "Chưa có quyền Shizuku"

        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            process.waitFor()

            if (error.isNotBlank()) "$output\n[LỖI]: $error" else output.ifBlank { "OK" }
        } catch (e: Exception) {
            "Lỗi khi chạy lệnh: ${e.message}"
        }
    }
}
