package com.lumi.fpsaddons

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(), Refreshable {

    private lateinit var tvStatus: TextView
    private lateinit var featureButtons: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvStatus = view.findViewById(R.id.tvStatus)

        featureButtons = listOf(
            R.id.btnQuickFix, R.id.btnBoostScreen, R.id.btnOptimize, R.id.btnDpi,
            R.id.btnGameMode, R.id.btnSustainedPerf, R.id.btnBatterySaver, R.id.btnGpuRender
        ).map { view.findViewById<Button>(it) }

        view.findViewById<Button>(R.id.btnRequestPermission).setOnClickListener {
            if (!ShizukuHelper.isShizukuAvailable()) {
                LogManager.log("Shizuku chưa chạy — mở app Shizuku, bật service trước.")
            } else {
                ShizukuHelper.requestPermission()
            }
        }

        view.findViewById<Button>(R.id.btnQuickFix).setOnClickListener {
            LogManager.log("Đang chạy Fix Lag FPS...")
            LogManager.log(ShellOptimizer.quickFixLag())
        }

        view.findViewById<Button>(R.id.btnBoostScreen).setOnClickListener {
            LogManager.log(ShellOptimizer.optimizeScreen())
        }

        view.findViewById<Button>(R.id.btnOptimize).setOnClickListener {
            LogManager.log(OptimizeTasks.disableSystemAnimations())
        }

        view.findViewById<Button>(R.id.btnDpi).setOnClickListener { showDpiDialog() }

        view.findViewById<Button>(R.id.btnGameMode).setOnClickListener {
            LogManager.log(ShellOptimizer.enableGamePerformanceMode())
        }

        view.findViewById<Button>(R.id.btnSustainedPerf).setOnClickListener {
            LogManager.log(ShellOptimizer.enableSustainedPerformance())
        }

        view.findViewById<Button>(R.id.btnBatterySaver).setOnClickListener {
            LogManager.log(ShellOptimizer.disableBatterySaver())
        }

        view.findViewById<Button>(R.id.btnGpuRender).setOnClickListener {
            LogManager.log(ShellOptimizer.forceGpuRendering())
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun refresh() {
        val granted = ShizukuHelper.hasPermission()
        tvStatus.text = when {
            !ShizukuHelper.isShizukuAvailable() -> "Trạng thái: Shizuku chưa chạy"
            granted -> "Trạng thái: Đã có quyền ✅"
            else -> "Trạng thái: Chưa cấp quyền — mọi tính năng đang khoá"
        }
        featureButtons.forEach {
            it.isEnabled = granted
            if (granted) {
                it.setBackgroundResource(R.drawable.btn_neon)
                it.paintFlags = it.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                it.setTextColor(requireContext().getColor(R.color.text_light))
            } else {
                it.setBackgroundResource(R.drawable.btn_disabled)
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                it.setTextColor(Color.parseColor("#FF3B30"))
            }
        }
    }

    private fun showDpiDialog() {
        val input = EditText(requireContext())
        input.hint = "Ví dụ: 400 (mặc định máy thường 400-480)"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setTextColor(requireContext().getColor(R.color.text_light))

        AlertDialog.Builder(requireContext(), R.style.LumiDialogTheme)
            .setTitle("Chỉnh DPI màn hình")
            .setMessage("DPI thấp hơn = icon/chữ nhỏ lại.")
            .setView(input)
            .setPositiveButton("Áp dụng") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value == null || value < 120 || value > 640) {
                    LogManager.log("DPI không hợp lệ — nhập số từ 120 đến 640.")
                } else {
                    LogManager.log(DpiHelper.setDensity(value))
                }
            }
            .setNeutralButton("Đặt lại mặc định") { _, _ -> LogManager.log(DpiHelper.resetDensity()) }
            .setNegativeButton("Huỷ", null)
            .show()
    }
}
