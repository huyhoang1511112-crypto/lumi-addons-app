package com.lumi.fpsaddons

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var prefs: android.content.SharedPreferences

    private var useShizuku = true

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        updateStatus()
        log(if (grantResult == 0) "Đã được cấp quyền Shizuku ✅" else "Bị từ chối quyền ❌")
    }

    private val pickAddonLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val name = queryFileName(uri) ?: "addon_${System.currentTimeMillis()}.mcpack"
            log("Đang cài đặt: $name ...")
            val result = if (useShizuku) {
                AddonInstaller.installViaShizuku(this, uri, name)
            } else {
                AddonInstaller.installViaFileAccess(uri, this, name)
            }
            log(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("lumi_prefs", MODE_PRIVATE)
        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        setupButtons()

        if (!prefs.contains("use_shizuku")) {
            showModeSelectDialog()
        } else {
            useShizuku = prefs.getBoolean("use_shizuku", true)
            applyModeUI()
        }

        updateStatus()
    }

    private fun showModeSelectDialog() {
        AlertDialog.Builder(this)
            .setTitle("Chọn chế độ hoạt động")
            .setMessage(
                "• Dùng Shizuku: đầy đủ tính năng, cần cài app Shizuku riêng.\n\n" +
                "• Không dùng Shizuku: dùng được trên mọi máy kể cả Android đời thấp, " +
                "nhưng một số tính năng nâng cao (Dọn RAM, Chỉnh DPI, Tắt animation) sẽ bị khoá."
            )
            .setCancelable(false)
            .setPositiveButton("Dùng Shizuku") { _, _ -> saveMode(true) }
            .setNegativeButton("Không dùng Shizuku") { _, _ -> saveMode(false) }
            .show()
    }

    private fun saveMode(shizuku: Boolean) {
        useShizuku = shizuku
        prefs.edit().putBoolean("use_shizuku", shizuku).apply()
        applyModeUI()
        log(if (shizuku) "Đã chọn chế độ: Dùng Shizuku" else "Đã chọn chế độ: Không dùng Shizuku")
    }

    private fun applyModeUI() {
        val btnCleanRam = findViewById<Button>(R.id.btnCleanRam)
        val btnOptimize = findViewById<Button>(R.id.btnOptimize)
        val btnDpi = findViewById<Button>(R.id.btnDpi)
        val btnRequestPermission = findViewById<Button>(R.id.btnRequestPermission)

        val lockedButtons = listOf(btnCleanRam, btnOptimize, btnDpi)

        if (useShizuku) {
            lockedButtons.forEach { it.isEnabled = true; it.setBackgroundResource(R.drawable.btn_neon) }
            btnRequestPermission.visibility = android.view.View.VISIBLE
        } else {
            lockedButtons.forEach {
                it.isEnabled = false
                it.setBackgroundResource(R.drawable.btn_disabled)
                it.text = "${it.text} ✗"
            }
            btnRequestPermission.visibility = android.view.View.GONE
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

        findViewById<Button>(R.id.btnImportAddon).setOnClickListener {
            if (useShizuku) {
                if (!ShizukuHelper.hasPermission()) {
                    log("Cần xin quyền Shizuku trước khi nhập addon.")
                    return@setOnClickListener
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    log("Cần cấp quyền 'Quản lý toàn bộ file' trước — đang mở cài đặt...")
                    requestAllFilesAccess()
                    return@setOnClickListener
                }
            }
            pickAddonLauncher.launch("*/*")
        }

        findViewById<Button>(R.id.btnCleanRam).setOnClickListener {
            if (!ShizukuHelper.hasPermission()) {
                log("Cần xin quyền Shizuku trước khi dọn RAM.")
            } else {
                showCleanRamDialog()
            }
        }

        findViewById<Button>(R.id.btnClearCache).setOnClickListener {
            log(OptimizeTasks.clearMinecraftCache())
        }

        findViewById<Button>(R.id.btnOptimize).setOnClickListener {
            log(OptimizeTasks.disableSystemAnimations())
        }

        findViewById<Button>(R.id.btnDpi).setOnClickListener {
            showDpiDialog()
        }
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun showCleanRamDialog() {
        val names = RamCleaner.COMMON_APPS.keys.toTypedArray()
        val packages = RamCleaner.COMMON_APPS.values.toTypedArray()
        val checked = BooleanArray(names.size) { true }

        AlertDialog.Builder(this)
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

    private fun showDpiDialog() {
        val input = EditText(this)
        input.hint = "Ví dụ: 400 (mặc định máy thường 400-480)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Chỉnh DPI màn hình")
            .setMessage("DPI thấp hơn = icon/chữ nhỏ lại, có thể mượt hơn với vài app. Để trống rồi bấm 'Đặt lại mặc định' nếu muốn quay về ban đầu.")
            .setView(input)
            .setPositiveButton("Áp dụng") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value == null || value < 120 || value > 640) {
                    log("DPI không hợp lệ — nhập số từ 120 đến 640.")
                } else {
                    log(DpiHelper.setDensity(value))
                }
            }
            .setNeutralButton("Đặt lại mặc định") { _, _ ->
                log(DpiHelper.resetDensity())
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun updateStatus() {
        tvStatus.text = when {
            !useShizuku -> "Chế độ: Không dùng Shizuku"
            !ShizukuHelper.isShizukuAvailable() -> "Trạng thái: Shizuku chưa chạy"
            ShizukuHelper.hasPermission() -> "Trạng thái: Đã có quyền ✅"
            else -> "Trạng thái: Chưa cấp quyền"
        }
    }

    private fun log(msg: String) {
        tvLog.append("\n$msg")
    }

    private fun queryFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
        }
        return null
    }
}
