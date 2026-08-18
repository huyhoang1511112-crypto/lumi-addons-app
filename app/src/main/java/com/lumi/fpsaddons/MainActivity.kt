package com.lumi.fpsaddons

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var featureButtons: List<Button>
    private lateinit var btnComboBoost: Button

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        applyPermissionUI()
        log(if (grantResult == 0) "Đã được cấp quyền Shizuku ✅" else "Bị từ chối quyền ❌")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)
        btnComboBoost = findViewById(R.id.btnComboBoost)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        featureButtons = listOf(
            R.id.btnQuickFix, R.id.btnBoostScreen, R.id.btnCleanRam, R.id.btnClearCache,
            R.id.btnOptimize, R.id.btnDpi, R.id.btnGameMode, R.id.btnSustainedPerf,
            R.id.btnBatterySaver, R.id.btnGpuRender, R.id.btnComboBoost
        ).map { findViewById<Button>(it) }

        setupButtons()
        featureButtons.forEach { it.addPressAnimation() }
        findViewById<Button>(R.id.btnRequestPermission).addPressAnimation()

        applyPermissionUI()
        updateStatus()
        updateComboBoostButtonText()
    }

    override fun onResume() {
        super.onResume()
        applyPermissionUI()
        updateStatus()
        updateComboBoostButtonText()
    }

    private fun applyPermissionUI() {
        val granted = ShizukuHelper.hasPermission()
        featureButtons.forEach {
            it.isEnabled = granted
            if (granted) {
                it.setBackgroundResource(R.drawable.btn_neon)
                it.paintFlags = it.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                it.setTextColor(getColor(R.color.text_light))
            } else {
                it.setBackgroundResource(R.drawable.btn_disabled)
                it.paintFlags = it.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                it.setTextColor(Color.parseColor("#FF3B30"))
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnRequestPermission).setOnClickListener {
            if (!ShizukuHelper.isShizukuAvailable()) {
                log("Shizuku chưa chạy — mở app Shizuku, bật service trước.")
            } else {
                ShizukuHelper.requestPermission()
            }
        }

        findViewById<Button>(R.id.btnQuickFix).setOnClickListener {
            log("Đang chạy Fix Lag FPS...")
            log(ShellOptimizer.quickFixLag())
        }

        findViewById<Button>(R.id.btnBoostScreen).setOnClickListener {
            log(ShellOptimizer.boostRefreshRate())
        }

        findViewById<Button>(R.id.btnCleanRam).setOnClickListener { showCleanRamDialog() }

        findViewById<Button>(R.id.btnClearCache).setOnClickListener { showCacheClearDialog() }

        findViewById<Button>(R.id.btnOptimize).setOnClickListener {
            log(OptimizeTasks.disableSystemAnimations())
        }

        findViewById<Button>(R.id.btnDpi).setOnClickListener { showDpiDialog() }

        findViewById<Button>(R.id.btnGameMode).setOnClickListener {
            log(ShellOptimizer.enableGamePerformanceMode())
        }

        findViewById<Button>(R.id.btnSustainedPerf).setOnClickListener {
            log(ShellOptimizer.enableSustainedPerformance())
        }

        findViewById<Button>(R.id.btnBatterySaver).setOnClickListener {
            log(ShellOptimizer.disableBatterySaver())
        }

        findViewById<Button>(R.id.btnGpuRender).setOnClickListener {
            log(ShellOptimizer.forceGpuRendering())
        }

        btnComboBoost.setOnClickListener { toggleComboBoost() }
    }

    private fun toggleComboBoost() {
        if (AntiGhostTouch.isRunning()) {
            log(ShellOptimizer.stopComboBoost())
        } else {
            val points = listOf(
                AntiGhostTouch.TapPoint(3005, 788),
                AntiGhostTouch.TapPoint(3000, 1000),
                AntiGhostTouch.TapPoint(2729, 1061)
            )
            log(
                ShellOptimizer.startComboBoost(
                    refreshRate = 120f,
                    touchPoints = points,
                    touchIntervalMs = 3000,
                    scope = lifecycleScope
                )
            )
        }
        updateComboBoostButtonText()
    }

    private fun updateComboBoostButtonText() {
        btnComboBoost.text = if (AntiGhostTouch.isRunning())
            "Tắt Combo Boost" else "Bật Combo Boost (Buff Màn + Chống liệt)"
    }

    private fun showCleanRamDialog() {
        val names = RamCleaner.COMMON_APPS.keys.toTypedArray()
        val packages = RamCleaner.COMMON_APPS.values.toTypedArray()
        val checked = BooleanArray(names.size) { true }

        AlertDialog.Builder(this, R.style.LumiDialogTheme)
            .setTitle("Chọn app muốn tắt để giải phóng RAM")
            .setMultiChoiceItems(names, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Dọn ngay") { _, _ ->
                val selected = packages.filterIndexed { i, _ -> checked[i] }
                log("Đang dọn RAM...")
                log(RamCleaner.clean(selected))
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun showCacheClearDialog() {
        val allApps = RamCleaner.COMMON_APPS + mapOf("Free Fire" to "com.dts.freefireth")
        val names = allApps.keys.toList()
        val packages = allApps.values.toList()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val selectAllBox = CheckBox(this).apply {
            text = "Chọn tất cả"
            setTextColor(getColor(R.color.neon_pink))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(selectAllBox)

        val divider = View(this).apply { setBackgroundColor(getColor(R.color.neon_pink_deep)) }
        container.addView(divider, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
            topMargin = 16; bottomMargin = 16
        })

        val checkboxes = names.map { name ->
            CheckBox(this).apply { text = name; setTextColor(getColor(R.color.text_light)) }
        }
        checkboxes.forEach { container.addView(it) }

        selectAllBox.setOnCheckedChangeListener { _, isChecked -> checkboxes.forEach { it.isChecked = isChecked } }

        val scroll = ScrollView(this).apply { addView(container) }

        AlertDialog.Builder(this, R.style.LumiDialogTheme)
            .setTitle("Chọn app muốn xoá cache")
            .setView(scroll)
            .setPositiveButton("Xoá cache") { _, _ ->
                val selected = packages.filterIndexed { i, _ -> checkboxes[i].isChecked }
                if (selected.isEmpty()) {
                    log("Chưa chọn app nào.")
                } else {
                    log("Đang xoá cache...")
                    log(OptimizeTasks.clearCacheForApps(selected))
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun showDpiDialog() {
        val input = EditText(this)
        input.hint = "Ví dụ: 400 (mặc định máy thường 400-480)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setTextColor(getColor(R.color.text_light))

        AlertDialog.Builder(this, R.style.LumiDialogTheme)
            .setTitle("Chỉnh DPI màn hình")
            .setMessage("DPI thấp hơn = icon/chữ nhỏ lại. Để trống rồi bấm 'Đặt lại mặc định' nếu muốn quay về ban đầu.")
            .setView(input)
            .setPositiveButton("Áp dụng") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value == null || value < 120 || value > 640) {
                    log("DPI không hợp lệ — nhập số từ 120 đến 640.")
                } else {
                    log(DpiHelper.setDensity(value))
                }
            }
            .setNeutralButton("Đặt lại mặc định") { _, _ -> log(DpiHelper.resetDensity()) }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        AntiGhostTouch.stop()
        super.onDestroy()
    }

    private fun updateStatus() {
        tvStatus.text = when {
            !ShizukuHelper.isShizukuAvailable() -> "Trạng thái: Shizuku chưa chạy"
            ShizukuHelper.hasPermission() -> "Trạng thái: Đã có quyền ✅"
            else -> "Trạng thái: Chưa cấp quyền — mọi tính năng đang khoá"
        }
    }

    private fun log(msg: String) {
        tvLog.append("\n$msg")
    }
}

fun View.addPressAnimation() {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }
        false
    }
}
