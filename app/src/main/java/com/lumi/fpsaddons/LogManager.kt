package com.lumi.fpsaddons

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Nhật ký hoạt động dùng chung cho toàn app — mọi Fragment đều ghi log
 * vào đây, có kèm giờ:phút:giây ngày/tháng/năm. Tab "Log" sẽ hiển thị
 * toàn bộ lịch sử này.
 */
object LogManager {
    private val entries = mutableListOf<String>()
    private var listener: ((String) -> Unit)? = null

    fun setListener(l: ((String) -> Unit)?) {
        listener = l
    }

    fun log(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale("vi")).format(Date())
        val line = "[$time] $msg"
        entries.add(line)
        listener?.invoke(line)
    }

    fun getAll(): List<String> = entries.toList()

    fun clear() {
        entries.clear()
    }
}
