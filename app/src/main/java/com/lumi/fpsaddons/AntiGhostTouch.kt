package com.lumi.fpsaddons

import kotlinx.coroutines.*

/**
 * Chống "ghost touch" / cảm ứng chai ở 1 vùng màn hình:
 * tự động tap nhẹ tại các toạ độ cố định theo chu kỳ để giữ vùng cảm ứng
 * đó không bị đơ. CHỈ nên bật khi không dùng máy (để bàn, sạc) — 
 * KHÔNG bật lúc đang thao tác/chơi game vì tap ảo sẽ đè lên tap thật.
 */
object AntiGhostTouch {

    private var job: Job? = null

    // Đổi holdMs thành mặc định 500ms để giả lập thao tác "Nhấp lâu" từ ảnh
    data class TapPoint(val x: Int, val y: Int, val holdMs: Long = 500)

    fun start(scope: CoroutineScope, intervalMs: Long = 3000) {
        stop()
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                // 1. Tương tác UI: Nhấp lâu (3005, 788)
                ShizukuHelper.runCommand("input swipe 3005 788 3005 788 500")
                
                // Chờ 85 mili giây
                delay(85)

                // 2. Tương tác UI: Nhấp lâu (3000, 1000)
                ShizukuHelper.runCommand("input swipe 3000 1000 3000 1000 500")
                
                // Chờ 111 mili giây
                delay(111)

                // 3. Tương tác UI: Nhấp lâu (2729, 1061)
                ShizukuHelper.runCommand("input swipe 2729 1061 2729 1061 500")

                // Nghỉ giữa các chu kỳ lặp lại vòng macro
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean = job?.isActive == true
}

