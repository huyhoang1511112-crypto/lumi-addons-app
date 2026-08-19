package com.lumi.fpsaddons

/**
 * Đo FPS THỰC của app đang hiển thị trên màn hình, dựa trên timestamp
 * khung hình thật do hệ thống Android ghi lại (dumpsys SurfaceFlinger
 * --latency) — đây là lệnh chuẩn, các app đo FPS phổ biến đều dùng cách này.
 */
object FpsMeasurer {

    data class FpsResult(val fps: Double, val note: String)

    private fun getForegroundWindowName(): String {
        val raw = ShizukuHelper.runCommand("dumpsys window windows | grep -E 'mCurrentFocus'")
        val regex = Regex("\\{.*?\\s(\\S+)\\}")
        return regex.find(raw)?.groupValues?.get(1) ?: ""
    }

    fun measureFps(): FpsResult {
        val window = getForegroundWindowName()
        if (window.isBlank()) {
            return FpsResult(0.0, "Không xác định được app đang mở — hãy mở app/game cần đo trước.")
        }

        val raw = ShizukuHelper.runCommand("dumpsys SurfaceFlinger --latency \"$window\"")
        val lines = raw.lines().drop(1)
        val presentTimes = lines.mapNotNull { line ->
            val cols = line.trim().split(Regex("\\s+"))
            if (cols.size >= 2) cols[1].toLongOrNull() else null
        }.filter { it != 0L && it != Long.MAX_VALUE }.sorted()

        if (presentTimes.size < 2) {
            return FpsResult(0.0, "Chưa đủ dữ liệu khung hình — thử lại trong lúc app/game đang chạy.")
        }

        val deltas = presentTimes.zipWithNext { a, b -> b - a }.filter { it > 0 }
        if (deltas.isEmpty()) return FpsResult(0.0, "Không tính được, thử lại.")

        val avgDeltaNs = deltas.average()
        val fps = 1_000_000_000.0 / avgDeltaNs
        return FpsResult(fps, "Đo trên: $window (${deltas.size} khung hình mẫu)")
    }
}
