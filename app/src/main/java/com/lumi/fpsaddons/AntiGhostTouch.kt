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

    data class TapPoint(val x: Int, val y: Int, val holdMs: Long = 50)

    fun start(points: List<TapPoint>, intervalMs: Long = 3000, scope: CoroutineScope) {
        stop()
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                for (p in points) {
                    ShizukuHelper.runCommand("input tap ${p.x} ${p.y}")
                    delay(p.holdMs)
                }
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
